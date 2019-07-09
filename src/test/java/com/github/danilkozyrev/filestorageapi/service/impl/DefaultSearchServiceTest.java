package com.github.danilkozyrev.filestorageapi.service.impl;

import com.github.danilkozyrev.filestorageapi.domain.File;
import com.github.danilkozyrev.filestorageapi.domain.Folder;
import com.github.danilkozyrev.filestorageapi.dto.form.SearchForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import com.github.danilkozyrev.filestorageapi.mapper.ItemProbeMapper;
import com.github.danilkozyrev.filestorageapi.mapper.MetadataMapper;
import com.github.danilkozyrev.filestorageapi.persistence.*;
import com.github.danilkozyrev.filestorageapi.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Example;

import java.time.Instant;
import java.util.List;

import static com.github.danilkozyrev.filestorageapi.util.EntityBuilders.*;
import static com.github.danilkozyrev.filestorageapi.util.FormBuilders.*;
import static com.github.danilkozyrev.filestorageapi.util.ProjectionBuilders.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSearchServiceTest {

    @Mock private FolderRepository folderRepository;
    @Mock private FileRepository fileRepository;
    @Mock private UserRepository userRepository;
    @Mock private ItemProbeMapper itemProbeMapper;
    @Mock private MetadataMapper metadataMapper;

    @InjectMocks private DefaultSearchService searchService;

    @Captor private ArgumentCaptor<Example<File>> fileExampleCaptor;
    @Captor private ArgumentCaptor<Example<Folder>> folderExampleCaptor;

    @Test
    @SuppressWarnings("unchecked")
    public void findItems_whenMimeTypeNull_shouldReturnFoundItems() {
        long ownerId = 0L;
        SearchForm searchForm = defaultSearchForm().mimeType(null).build();

        File fileProbe = defaultFile().id(1L).build();
        Folder folderProbe = defaultFolder().id(2L).build();

        List<File> foundFiles = List.of(
                defaultFile().id(3L).build(),
                defaultFile().id(4L).build());
        List<Folder> foundFolders = List.of(
                defaultFolder().id(5L).build(),
                defaultFolder().id(6L).build());

        List<Metadata> metadataList = List.of(
                ProjectionBuilders.defaultMetadata().type(Metadata.Type.FILE).id(7L).build(),
                ProjectionBuilders.defaultMetadata().type(Metadata.Type.FOLDER).id(8L).build());

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemProbeMapper.mapToFileProbe(searchForm, ownerId)).thenReturn(fileProbe);
        when(itemProbeMapper.mapToFolderProbe(searchForm, ownerId)).thenReturn(folderProbe);
        when(fileRepository.findAll(any(Example.class))).thenReturn(foundFiles);
        when(folderRepository.findAll(any(Example.class))).thenReturn(foundFolders);
        when(metadataMapper.mapItems(foundFolders, foundFiles)).thenReturn(metadataList);

        List<Metadata> returnedMetadataList = searchService.findItems(searchForm, ownerId);

        verify(fileRepository).findAll(fileExampleCaptor.capture());
        verify(folderRepository).findAll(folderExampleCaptor.capture());

        assertThat(fileExampleCaptor.getValue().getProbe(), equalTo(fileProbe));
        assertThat(folderExampleCaptor.getValue().getProbe(), equalTo(folderProbe));
        assertThat(returnedMetadataList, equalTo(metadataList));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findItems_whenMimeTypeNotNull_shouldReturnFoundFiles() {
        long ownerId = 0L;
        SearchForm searchForm = defaultSearchForm().build();
        File fileProbe = defaultFile().id(1L).build();
        List<File> foundFiles = List.of(defaultFile().id(2L).build(), defaultFile().id(3L).build());
        List<Metadata> metadataList = List.of(
                defaultMetadata().type(Metadata.Type.FILE).id(4L).build(),
                defaultMetadata().type(Metadata.Type.FILE).id(5L).build());

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemProbeMapper.mapToFileProbe(searchForm, ownerId)).thenReturn(fileProbe);
        when(fileRepository.findAll(any(Example.class))).thenReturn(foundFiles);
        when(metadataMapper.mapFiles(foundFiles)).thenReturn(metadataList);

        List<Metadata> returnedMetadataList = searchService.findItems(searchForm, ownerId);

        verify(fileRepository).findAll(fileExampleCaptor.capture());
        verifyZeroInteractions(folderRepository);

        assertThat(fileExampleCaptor.getValue().getProbe(), equalTo(fileProbe));
        assertThat(returnedMetadataList, equalTo(metadataList));
    }

    @Test(expected = RecordNotFoundException.class)
    public void findItems_whenOwnerNotFound_shouldThrowException() {
        long ownerId = 0L;
        SearchForm searchForm = FormBuilders.defaultSearchForm().build();
        when(userRepository.existsById(ownerId)).thenReturn(false);
        searchService.findItems(searchForm, ownerId);
    }

    @Test
    public void findRecentItems_whenOwnerFound_shouldReturnRecentItems() {
        long ownerId = 0L;
        Instant afterDate = Instant.EPOCH;

        List<File> foundFiles = List.of(
                defaultFile().id(1L).build(),
                defaultFile().id(2L).build());
        List<Folder> foundFolders = List.of(
                defaultFolder().id(3L).build(),
                defaultFolder().id(4L).build());

        List<Metadata> metadataList = List.of(
                defaultMetadata().type(Metadata.Type.FILE).id(5L).build(),
                defaultMetadata().type(Metadata.Type.FOLDER).id(6L).build());

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(fileRepository.findFilesByDateModifiedAfterAndOwnerId(afterDate, ownerId)).thenReturn(foundFiles);
        when(folderRepository.findFoldersByDateModifiedAfterAndOwnerId(afterDate, ownerId)).thenReturn(foundFolders);
        when(metadataMapper.mapItems(foundFolders, foundFiles)).thenReturn(metadataList);

        List<Metadata> returnedMetadataList = searchService.findRecentItems(afterDate, ownerId);

        assertThat(returnedMetadataList, equalTo(metadataList));
    }

    @Test(expected = RecordNotFoundException.class)
    public void findRecentItems_whenOwnerFound_shouldThrowException() {
        long ownerId = 0L;
        Instant afterDate = Instant.EPOCH;
        when(userRepository.existsById(ownerId)).thenReturn(false);
        searchService.findRecentItems(afterDate, ownerId);
    }

}
