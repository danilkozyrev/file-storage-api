package com.github.danilkozyrev.filestorageapi.service.impl;

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
import com.github.danilkozyrev.filestorageapi.util.*;
import org.apache.tika.mime.MimeTypes;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

import static com.github.danilkozyrev.filestorageapi.util.EntityBuilders.*;
import static com.github.danilkozyrev.filestorageapi.util.FormBuilders.*;
import static com.github.danilkozyrev.filestorageapi.util.ProjectionBuilders.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFileServiceTest {

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock private FolderRepository folderRepository;
    @Mock private FileRepository fileRepository;
    @Mock private MetadataMapper metadataMapper;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ApplicationProperties properties;

    @InjectMocks private DefaultFileService fileService;

    @Captor private ArgumentCaptor<File> savedFileCaptor;
    @Captor private ArgumentCaptor<EntityCreationEvent<File>> fileCreationEventCaptor;
    @Captor private ArgumentCaptor<EntityDeletionEvent<File>> fileDeletionEventCaptor;

    @Test
    public void saveFile_whenParentFoundAndLimitIsNotExceeded_shouldSaveFile() throws IOException {
        byte[] fileContents = new byte[32];
        new Random().nextBytes(fileContents);

        MetadataForm metadataForm = defaultMetadataForm().name("test.txt").build();
        ByteArrayResource fileResource = new ByteArrayResource(fileContents);
        User owner = defaultUser().build();
        Folder parent = defaultFolder().owner(owner).build();
        File savedFile = defaultFile().build();
        Metadata metadata = defaultMetadata().build();

        when(folderRepository.findById(metadataForm.getParentId())).thenReturn(Optional.of(parent));
        when(fileRepository.calculateTotalFileSizeByOwnerId(owner.getId())).thenReturn(0L);
        when(properties.getBaseLimit()).thenReturn(Long.MAX_VALUE);
        when(properties.getBaseFolder()).thenReturn(temporaryFolder.getRoot().getPath());
        when(fileRepository.save(any())).thenReturn(savedFile);
        when(metadataMapper.mapFile(savedFile)).thenReturn(metadata);

        Metadata returnedMetadata = fileService.saveFile(metadataForm, fileResource);

        verify(fileRepository).save(savedFileCaptor.capture());
        verify(eventPublisher).publishEvent(fileCreationEventCaptor.capture());

        File createdFile = savedFileCaptor.getValue();
        assertThat(fileCreationEventCaptor.getValue().getCreatedEntities(), contains(createdFile));
        assertThat(createdFile.getName(), equalTo(metadataForm.getName()));
        assertThat(createdFile.getSize(), equalTo(Long.valueOf(fileContents.length)));
        assertThat(createdFile.getMimeType(), equalTo(MimeTypes.PLAIN_TEXT));
        assertThat(createdFile.getLocation(), is(notNullValue()));
        assertThat(createdFile.getParent(), equalTo(parent));
        assertThat(createdFile.getOwner(), equalTo(owner));

        Path createdFilePath = Paths.get(temporaryFolder.getRoot().getPath() + createdFile.getLocation());
        assertThat(Files.exists(createdFilePath), equalTo(true));
        assertThat(Files.readAllBytes(createdFilePath), equalTo(fileContents));

        assertThat(returnedMetadata, equalTo(metadata));
    }

    @Test(expected = RecordNotFoundException.class)
    public void saveFile_whenParentNotFound_shouldThrowException() {
        MetadataForm metadataForm = defaultMetadataForm().build();
        Resource fileResource = new ByteArrayResource(new byte[32]);

        when(folderRepository.findById(metadataForm.getParentId())).thenReturn(Optional.empty());

        fileService.saveFile(metadataForm, fileResource);
    }

    @Test(expected = StorageLimitExceededException.class)
    public void saveFile_whenLimitIsExceeded_shouldThrowException() {
        MetadataForm metadataForm = defaultMetadataForm().build();
        Resource fileResource = new ByteArrayResource(new byte[32]);
        User owner = defaultUser().build();
        Folder parent = defaultFolder().owner(owner).build();

        when(folderRepository.findById(metadataForm.getParentId())).thenReturn(Optional.of(parent));
        when(fileRepository.calculateTotalFileSizeByOwnerId(owner.getId())).thenReturn(0L);
        when(properties.getBaseLimit()).thenReturn(Long.MIN_VALUE);

        fileService.saveFile(metadataForm, fileResource);
    }

    @Test
    public void generateFileAccessToken_whenFileFound_shouldReturnJwt() {
        long fileId = 0L;
        long validitySeconds = 60L;
        String secret = "super secret";

        when(fileRepository.existsById(fileId)).thenReturn(true);
        when(properties.getFileTokenValidity()).thenReturn(validitySeconds);
        when(properties.getFileTokenSecret()).thenReturn(secret);

        String token = fileService.generateFileAccessToken(fileId);

        String subject = JwtUtils.verifyToken(token, secret);
        assertThat(Long.valueOf(subject), equalTo(fileId));
    }

    @Test(expected = RecordNotFoundException.class)
    public void generateFileAccessToken_whenFileNotFound_shouldThrowException() {
        long fileId = 0L;
        when(fileRepository.existsById(fileId)).thenReturn(false);
        fileService.generateFileAccessToken(fileId);
    }

    @Test
    public void getFileMetadata_whenFileFound_shouldReturnMetadata() {
        long fileId = 0L;
        File file = defaultFile().id(fileId).build();
        Metadata metadata = defaultMetadata().build();

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(metadataMapper.mapFile(file)).thenReturn(metadata);

        Metadata returnedMetadata = fileService.getFileMetadata(fileId);

        assertThat(returnedMetadata, equalTo(metadata));
    }

    @Test(expected = RecordNotFoundException.class)
    public void getFileMetadata_whenFileNotFound_shouldThrowException() {
        long fileId = 0L;
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());
        fileService.getFileMetadata(fileId);
    }

    @Test
    public void getFileContents_whenFileFound_shouldReturnFileSystemResource() {
        long fileId = 0L;
        File file = defaultFile().id(fileId).build();
        String baseFolder = "/base";

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(properties.getBaseFolder()).thenReturn(baseFolder);

        FileSystemResource fileResource = fileService.getFileContents(fileId);

        assertThat(fileResource.getPath(), equalTo(baseFolder + file.getLocation()));
        assertThat(fileResource.getFilename(), equalTo(file.getName()));
    }

    @Test(expected = RecordNotFoundException.class)
    public void getFileContents_whenFileNotFound_shouldThrowException() {
        long fileId = 0L;
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());
        fileService.getFileContents(fileId);
    }

    @Test
    public void getFileContents_whenTokenIsValid_shouldReturnFileSystemResource() {
        long fileId = 0L;
        File file = defaultFile().id(fileId).build();
        String baseFolder = "/base";
        Instant expiration = Instant.now().plus(1, ChronoUnit.HOURS);
        String secret = "super secret";
        String token = JwtUtils.generateToken(String.valueOf(fileId), expiration, secret);

        when(properties.getBaseFolder()).thenReturn(baseFolder);
        when(properties.getFileTokenSecret()).thenReturn(secret);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        FileSystemResource fileResource = fileService.getFileContents(token);

        assertThat(fileResource.getPath(), equalTo(baseFolder + file.getLocation()));
        assertThat(fileResource.getFilename(), equalTo(file.getName()));
    }

    @Test(expected = ExpiredFileTokenException.class)
    public void getFileContents_whenTokenExpired_shouldThrowException() {
        String subject = "subject";
        Instant expiration = Instant.now().minus(1, ChronoUnit.HOURS);
        String secret = "super secret";
        String token = JwtUtils.generateToken(subject, expiration, secret);

        when(properties.getFileTokenSecret()).thenReturn(secret);

        fileService.getFileContents(token);
    }

    @Test(expected = RecordNotFoundException.class)
    public void getFileContents_whenTokenIsNotValid_shouldThrowException() {
        when(properties.getFileTokenSecret()).thenReturn("super secret");
        fileService.getFileContents("invalid token");
    }

    @Test
    public void updateFileMetadata_whenFileFoundAndParentFound_shouldUpdateMetadata() {
        long fileId = 0L;
        File file = defaultFile().id(fileId).build();
        MetadataForm metadataForm = defaultMetadataForm().build();
        Folder newParent = defaultFolder().build();
        Metadata metadata = defaultMetadata().build();

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(folderRepository.findById(metadataForm.getParentId())).thenReturn(Optional.of(newParent));
        when(metadataMapper.mapFile(file)).thenReturn(metadata);

        Metadata returnedMetadata = fileService.updateFileMetadata(fileId, metadataForm);

        assertThat(file.getName(), equalTo(metadataForm.getName()));
        assertThat(file.getParent(), equalTo(newParent));

        assertThat(returnedMetadata, equalTo(metadata));
    }

    @Test(expected = RecordNotFoundException.class)
    public void updateFileMetadata_whenFileNotFound_shouldThrowException() {
        long fileId = 0L;
        MetadataForm metadataForm = defaultMetadataForm().build();

        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        fileService.updateFileMetadata(fileId, metadataForm);
    }

    @Test(expected = RecordNotFoundException.class)
    public void updateFileMetadata_whenParentNotFound_shouldThrowException() {
        long fileId = 0L;
        File file = defaultFile().id(fileId).build();
        MetadataForm metadataForm = defaultMetadataForm().build();

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(folderRepository.findById(metadataForm.getParentId())).thenReturn(Optional.empty());

        fileService.updateFileMetadata(fileId, metadataForm);
    }

    @Test
    public void deleteFile_whenFileFoundAndNotPermanent_shouldDisconnectFile() {
        long fileId = 0L;
        File file = defaultFile().id(fileId).build();
        Folder parent = defaultFolder().build();
        file.setParent(parent);
        parent.getFiles().add(file);

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        fileService.deleteFile(fileId, false);

        verify(fileRepository).findById(fileId);
        verifyNoMoreInteractions(fileRepository, eventPublisher);

        assertThat(file.getParent(), is(nullValue()));
        assertThat(parent.getFiles(), not(hasItem(file)));
    }

    @Test(expected = RecordNotFoundException.class)
    public void deleteFile_whenFileNotFoundAndNotPermanent_shouldThrowException() {
        long fileId = 0L;
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());
        fileService.deleteFile(fileId, false);
    }

    @Test
    public void deleteFile_whenFileFoundAndPermanent_shouldDeleteFile() {
        long fileId = 0L;
        File file = defaultFile().id(fileId).build();

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        fileService.deleteFile(fileId, true);

        verify(eventPublisher).publishEvent(fileDeletionEventCaptor.capture());
        verify(fileRepository).delete(file);

        Iterable<File> deletedFiles = fileDeletionEventCaptor.getValue().getDeletedEntities();
        assertThat(deletedFiles, contains(file));
    }

    @Test(expected = RecordNotFoundException.class)
    public void deleteFile_whenFileNotFoundAndPermanent_shouldThrowException() {
        long fileId = 0L;
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());
        fileService.deleteFile(fileId, true);
    }

}
