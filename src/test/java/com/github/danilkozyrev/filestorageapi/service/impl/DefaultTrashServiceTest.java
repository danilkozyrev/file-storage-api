package com.github.danilkozyrev.filestorageapi.service.impl;

import com.github.danilkozyrev.filestorageapi.domain.File;
import com.github.danilkozyrev.filestorageapi.domain.Folder;
import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.event.EntityDeletionEvent;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import com.github.danilkozyrev.filestorageapi.mapper.MetadataMapper;
import com.github.danilkozyrev.filestorageapi.persistence.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Set;

import static com.github.danilkozyrev.filestorageapi.util.EntityBuilders.*;
import static com.github.danilkozyrev.filestorageapi.util.ProjectionBuilders.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultTrashServiceTest {

    @Mock private FolderRepository folderRepository;
    @Mock private FileRepository fileRepository;
    @Mock private UserRepository userRepository;
    @Mock private MetadataMapper metadataMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private DefaultTrashService trashService;

    @Captor private ArgumentCaptor<EntityDeletionEvent<File>> fileDeletionEventCaptor;

    @Test
    public void getTrashItems_whenOwnerFound_shouldReturnTrashItems() {
        long ownerId = 0L;
        List<Folder> foldersInTrash = List.of(
                defaultFolder().id(1L).build(),
                defaultFolder().id(2L).build());
        List<File> filesInTrash = List.of(
                defaultFile().id(3L).build(),
                defaultFile().id(4L).build());
        List<Metadata> metadataList = List.of(
                defaultMetadata().type(Metadata.Type.FOLDER).id(5L).build(),
                defaultMetadata().type(Metadata.Type.FILE).id(6L).build());

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(folderRepository.findDisconnectedFoldersByOwnerId(ownerId)).thenReturn(foldersInTrash);
        when(fileRepository.findDisconnectedFilesByOwnerId(ownerId)).thenReturn(filesInTrash);
        when(metadataMapper.mapItems(foldersInTrash, filesInTrash)).thenReturn(metadataList);

        List<Metadata> returnedMetadataList = trashService.getTrashItems(ownerId);

        assertThat(returnedMetadataList, equalTo(metadataList));
    }

    @Test(expected = RecordNotFoundException.class)
    public void getTrashItems_whenOwnerNotFound_shouldThrowException() {
        long ownerId = 0L;
        when(userRepository.existsById(ownerId)).thenReturn(false);
        trashService.getTrashItems(ownerId);
    }

    @Test
    public void emptyTrash_whenOwnerFound_shouldDeleteItems() {
        long ownerId = 0L;
        List<Folder> foldersInTrash = List.of(
                defaultFolder().id(1L).build(),
                defaultFolder().id(2L).build());
        List<File> filesInTrash = List.of(
                defaultFile().id(3L).build(),
                defaultFile().id(4L).build());
        List<File> fileAncestors = List.of(
                defaultFile().id(5L).build(),
                defaultFile().id(6L).build());

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(folderRepository.findDisconnectedFoldersByOwnerId(ownerId)).thenReturn(foldersInTrash);
        when(fileRepository.findDisconnectedFilesByOwnerId(ownerId)).thenReturn(filesInTrash);
        when(fileRepository.deepFindAllFilesByParentIdIn(Set.of(1L, 2L))).thenReturn(fileAncestors);

        trashService.emptyTrash(ownerId);

        verify(eventPublisher).publishEvent(fileDeletionEventCaptor.capture());
        verify(folderRepository).deleteAll(foldersInTrash);
        verify(fileRepository).deleteAll(filesInTrash);

        Iterable<File> deletedFiles = fileDeletionEventCaptor.getValue().getDeletedEntities();
        assertThat(deletedFiles, containsInAnyOrder(
                filesInTrash.get(0), filesInTrash.get(1), fileAncestors.get(0), fileAncestors.get(1)));
    }

    @Test(expected = RecordNotFoundException.class)
    public void emptyTrash_whenOwnerNotFound_shouldThrowException() {
        long ownerId = 0L;
        when(userRepository.existsById(ownerId)).thenReturn(false);
        trashService.emptyTrash(ownerId);
    }

}
