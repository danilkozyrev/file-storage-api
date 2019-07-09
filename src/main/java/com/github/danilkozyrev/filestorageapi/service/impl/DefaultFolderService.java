package com.github.danilkozyrev.filestorageapi.service.impl;

import com.github.danilkozyrev.filestorageapi.domain.*;
import com.github.danilkozyrev.filestorageapi.dto.form.MetadataForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.event.EntityDeletionEvent;
import com.github.danilkozyrev.filestorageapi.exception.CircularFolderStructureException;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import com.github.danilkozyrev.filestorageapi.mapper.MetadataMapper;
import com.github.danilkozyrev.filestorageapi.persistence.FileRepository;
import com.github.danilkozyrev.filestorageapi.persistence.FolderRepository;
import com.github.danilkozyrev.filestorageapi.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DefaultFolderService implements FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final MetadataMapper metadataMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Metadata createFolder(MetadataForm metadataForm) {
        Folder folder = new Folder();
        folder.setName(metadataForm.getName());
        folder.setRoot(false);
        folder.setParent(getFolder(metadataForm.getParentId()));
        folder.setOwner(folder.getParent().getOwner());
        folder = folderRepository.save(folder);
        return metadataMapper.mapFolder(folder);
    }

    @Override
    @Transactional(readOnly = true)
    public Metadata getFolderMetadata(Long folderId) {
        Folder folder = getFolder(folderId);
        return metadataMapper.mapFolder(folder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Metadata> getFolderItems(Long folderId) {
        Folder folder = getFolder(folderId);
        return metadataMapper.mapItems(folder.getSubfolders(), folder.getFiles());
    }

    @Override
    @Transactional(readOnly = true)
    public Metadata getRootFolderMetadata(Long ownerId) {
        Folder rootFolder = getRoot(ownerId);
        return metadataMapper.mapFolder(rootFolder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Metadata> getRootFolderItems(Long ownerId) {
        Folder rootFolder = getRoot(ownerId);
        return metadataMapper.mapItems(rootFolder.getSubfolders(), rootFolder.getFiles());
    }

    @Override
    @Transactional
    public Metadata updateFolderMetadata(Long folderId, MetadataForm metadataForm) {
        Folder folder = getFolder(folderId);
        if (metadataForm.getParentId() != null) {
            Folder newParent = getFolder(metadataForm.getParentId());
            assertCircularReferenceNotOccurs(folder, newParent);
            folder.setParent(newParent);
        }
        if (metadataForm.getName() != null) {
            folder.setName(metadataForm.getName());
        }
        return metadataMapper.mapFolder(folder);
    }

    @Override
    @Transactional
    public void deleteFolder(Long folderId, boolean permanent) {
        Folder folder = getFolder(folderId);
        if (permanent) {
            List<File> fileAncestorList = fileRepository.deepFindAllFilesByParentIdIn(Set.of(folder.getId()));
            eventPublisher.publishEvent(new EntityDeletionEvent<>(this, fileAncestorList));
            folderRepository.delete(folder);
        } else if (folder.getParent() != null) {
            folder.getParent().getSubfolders().remove(folder);
            folder.setParent(null);
        }
    }

    private Folder getFolder(Long folderId) {
        return folderRepository
                .findById(folderId)
                .orElseThrow(() -> new RecordNotFoundException(Folder.class, folderId));
    }

    private Folder getRoot(Long ownerId) {
        return folderRepository
                .findRootFolderByOwnerId(ownerId)
                .orElseThrow(() -> new RecordNotFoundException(User.class, ownerId));
    }

    private void assertCircularReferenceNotOccurs(Folder folder, Folder newParent) {
        List<Folder> subfolders = folderRepository.deepFindAllSubfoldersByParentIdIn(Set.of(folder.getId()));
        for (Folder subfolder : subfolders) {
            if (subfolder.getId().equals(newParent.getId())) {
                throw new CircularFolderStructureException();
            }
        }
    }

}
