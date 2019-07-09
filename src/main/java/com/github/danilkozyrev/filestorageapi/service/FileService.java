package com.github.danilkozyrev.filestorageapi.service;

import com.github.danilkozyrev.filestorageapi.dto.form.MetadataForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.exception.*;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Provides operations with files.
 */
public interface FileService {

    /**
     * Saves a given file.
     *
     * @param metadataForm the metadata form.
     * @param fileResource the resource representing the file.
     * @return the metadata of the saved file.
     * @throws FileSystemException           if the file can't be saved.
     * @throws StorageLimitExceededException if the storage limit is exceeded.
     * @throws RecordNotFoundException       if the specified parent folder doesn't exist.
     */
    @PreAuthorize("hasPermission(#metadataForm.parentId, 'Folder', 'write')")
    Metadata saveFile(MetadataForm metadataForm, Resource fileResource);

    /**
     * Generates an expirable access token, that can be used for retrieving file contents.
     *
     * @param fileId the id of the file.
     * @return the generated token.
     * @throws RecordNotFoundException if the specified file doesn't exist.
     */
    @PreAuthorize("hasPermission(#fileId, 'File', 'read')")
    String generateFileAccessToken(Long fileId);

    /**
     * Retrieves file metadata.
     *
     * @param fileId the id of the file.
     * @return the metadata.
     * @throws RecordNotFoundException if the specified file doesn't exist.
     */
    @PreAuthorize("hasPermission(#fileId, 'File', 'read')")
    Metadata getFileMetadata(Long fileId);

    /**
     * Retrieves file contents.
     *
     * @param fileId the id of the file.
     * @return the resource representing the file.
     * @throws RecordNotFoundException if the specified file doesn't exist.
     */
    @PreAuthorize("hasPermission(#fileId, 'File', 'read')")
    Resource getFileContents(Long fileId);

    /**
     * Retrieves file contents using an access token.
     *
     * @param fileAccessToken the access token.
     * @return the resource representing the file.
     * @throws ExpiredFileTokenException if the token has expired.
     * @throws RecordNotFoundException   if the specified file doesn't exist or the access token is incorrect.
     */
    @PreAuthorize("permitAll()")
    Resource getFileContents(String fileAccessToken);

    /**
     * Partially updates metadata of a file. Allows to rename and/or move files.
     *
     * @param fileId       the id of the file.
     * @param metadataForm the metadata form.
     * @return the updated metadata.
     * @throws RecordNotFoundException if the specified file/parent folder doesn't exist.
     */
    @PreAuthorize("(hasPermission(#fileId, 'File', 'edit') " +
            "and (#metadataForm.parentId eq null ? true : hasPermission(#metadataForm.parentId, 'Folder', 'write')))")
    Metadata updateFileMetadata(Long fileId, MetadataForm metadataForm);

    /**
     * Deletes a file.
     *
     * @param fileId    the id of the file.
     * @param permanent delete permanently or put in the trash.
     * @throws RecordNotFoundException if the specified file doesn't exist.
     */
    @PreAuthorize("hasPermission(#fileId, 'File', 'delete')")
    void deleteFile(Long fileId, boolean permanent);

}
