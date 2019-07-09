package com.github.danilkozyrev.filestorageapi.service.impl;

import com.github.danilkozyrev.filestorageapi.domain.*;
import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.event.EntityDeletionEvent;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import com.github.danilkozyrev.filestorageapi.mapper.MetadataMapper;
import com.github.danilkozyrev.filestorageapi.persistence.*;
import com.github.danilkozyrev.filestorageapi.service.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultTrashService implements TrashService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final MetadataMapper metadataMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<Metadata> getTrashItems(Long ownerId) {
        if (userRepository.existsById(ownerId)) {
            List<Folder> foldersInTrash = folderRepository.findDisconnectedFoldersByOwnerId(ownerId);
            List<File> filesInTrash = fileRepository.findDisconnectedFilesByOwnerId(ownerId);
            return metadataMapper.mapItems(foldersInTrash, filesInTrash);
        } else {
            throw new RecordNotFoundException(User.class, ownerId);
        }
    }

    @Override
    @Transactional
    public void emptyTrash(Long ownerId) {
        if (userRepository.existsById(ownerId)) {
            List<Folder> foldersInTrash = folderRepository.findDisconnectedFoldersByOwnerId(ownerId);
            List<File> filesInTrash = fileRepository.findDisconnectedFilesByOwnerId(ownerId);

            // Check if the folder array is empty to avoid exceptions on sql query execution.
            List<File> filesToDelete = new ArrayList<>(filesInTrash);
            if (!foldersInTrash.isEmpty()) {
                // Find all child files of all folders in trash for further file content deletion.
                Set<Long> folderIdSet = foldersInTrash.stream().map(Folder::getId).collect(Collectors.toSet());
                filesToDelete.addAll(fileRepository.deepFindAllFilesByParentIdIn(folderIdSet));
            }

            eventPublisher.publishEvent(new EntityDeletionEvent<>(this, filesToDelete));
            folderRepository.deleteAll(foldersInTrash);
            fileRepository.deleteAll(filesInTrash);
        } else {
            throw new RecordNotFoundException(User.class, ownerId);
        }
    }

}
