package com.github.danilkozyrev.filestorageapi.service.impl;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.github.danilkozyrev.filestorageapi.config.ApplicationProperties;
import com.github.danilkozyrev.filestorageapi.domain.*;
import com.github.danilkozyrev.filestorageapi.dto.form.MetadataForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.event.EntityCreationEvent;
import com.github.danilkozyrev.filestorageapi.event.EntityDeletionEvent;
import com.github.danilkozyrev.filestorageapi.exception.*;
import com.github.danilkozyrev.filestorageapi.mapper.MetadataMapper;
import com.github.danilkozyrev.filestorageapi.persistence.FileRepository;
import com.github.danilkozyrev.filestorageapi.persistence.FolderRepository;
import com.github.danilkozyrev.filestorageapi.service.FileService;
import com.github.danilkozyrev.filestorageapi.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultFileService implements FileService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final MetadataMapper metadataMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationProperties properties;

    /**
     * {@inheritDoc}
     * Uses serializable transaction isolation level to avoid saving another file during storage limit check. File
     * location is saved without the base folder, so this folder can be changed later.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Metadata saveFile(MetadataForm metadataForm, Resource fileResource) {
        try {
            Folder parent = getParent(metadataForm.getParentId());
            assertLimitIsNotExceeded(fileResource.contentLength(), parent.getOwner());

            File file = new File();
            file.setName(metadataForm.getName());
            file.setSize(fileResource.contentLength());
            file.setMimeType(FileSystemUtils.detectContentType(file.getName(), fileResource));
            file.setLocation(generateLocation());
            file.setParent(parent);
            file.setOwner(parent.getOwner());

            eventPublisher.publishEvent(new EntityCreationEvent<>(this, List.of(file)));
            FileSystemUtils.saveFile(fileResource, properties.getBaseFolder() + file.getLocation());
            file = fileRepository.save(file);
            return metadataMapper.mapFile(file);
        } catch (IOException exception) {
            throw new FileSystemException(exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String generateFileAccessToken(Long fileId) {
        if (fileRepository.existsById(fileId)) {
            Instant expiration = Instant.now().plus(properties.getFileTokenValidity(), ChronoUnit.SECONDS);
            String secret = properties.getFileTokenSecret();
            return JwtUtils.generateToken(fileId.toString(), expiration, secret);
        } else {
            throw new RecordNotFoundException(File.class, fileId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Metadata getFileMetadata(Long fileId) {
        File file = getFile(fileId);
        return metadataMapper.mapFile(file);
    }

    @Override
    @Transactional(readOnly = true)
    public FileSystemResource getFileContents(Long fileId) {
        File file = getFile(fileId);
        return new FileSystemResource(properties.getBaseFolder() + file.getLocation()) {
            @Override
            public String getFilename() {
                return file.getName();
            }
        };
    }

    @Override
    @Transactional(readOnly = true)
    public FileSystemResource getFileContents(String fileAccessToken) {
        try {
            String subject = JwtUtils.verifyToken(fileAccessToken, properties.getFileTokenSecret());
            return getFileContents(Long.valueOf(subject));
        } catch (TokenExpiredException exception) {
            throw new ExpiredFileTokenException();
        } catch (JWTVerificationException exception) {
            throw new RecordNotFoundException(File.class);
        }
    }

    @Override
    @Transactional
    public Metadata updateFileMetadata(Long fileId, MetadataForm metadataForm) {
        File file = getFile(fileId);
        if (metadataForm.getParentId() != null) {
            Folder newParentFolder = getParent(metadataForm.getParentId());
            file.setParent(newParentFolder);
        }
        if (metadataForm.getName() != null) {
            file.setName(metadataForm.getName());
        }
        return metadataMapper.mapFile(file);
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId, boolean permanent) {
        File file = getFile(fileId);
        if (permanent) {
            eventPublisher.publishEvent(new EntityDeletionEvent<>(this, List.of(file)));
            fileRepository.delete(file);
        } else if (file.getParent() != null) {
            file.getParent().getFiles().remove(file);
            file.setParent(null);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleFileCreationEvent(EntityCreationEvent<File> event) {
        for (File file : event.getCreatedEntities()) {
            FileSystemUtils.deleteFile(properties.getBaseFolder() + file.getLocation());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFileDeletionEvent(EntityDeletionEvent<File> event) {
        for (File file : event.getDeletedEntities()) {
            FileSystemUtils.deleteFile(properties.getBaseFolder() + file.getLocation());
        }
    }

    private File getFile(Long fileId) {
        return fileRepository
                .findById(fileId)
                .orElseThrow(() -> new RecordNotFoundException(File.class, fileId));
    }

    private Folder getParent(Long parentId) {
        return folderRepository
                .findById(parentId)
                .orElseThrow(() -> new RecordNotFoundException(Folder.class, parentId));
    }

    private void assertLimitIsNotExceeded(long newFileSize, User owner) {
        long usedSpace = fileRepository.calculateTotalFileSizeByOwnerId(owner.getId());
        Long baseLimit = properties.getBaseLimit();
        if (usedSpace + newFileSize > baseLimit) {
            throw new StorageLimitExceededException("Maximum storage limit " + baseLimit + " is exceeded");
        }
    }

    private static String generateLocation() {
        String uuidFileName = UUID.randomUUID().toString().replace("-", "");
        String separator = FileSystems.getDefault().getSeparator();
        return new StringBuilder()
                .append(separator)
                .append(uuidFileName, 0, 2)
                .append(separator)
                .append(uuidFileName, 2, 4)
                .append(separator)
                .append(uuidFileName, 4, 6)
                .toString();
    }

}
