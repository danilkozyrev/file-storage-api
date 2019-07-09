package com.github.danilkozyrev.filestorageapi.service;

import com.github.danilkozyrev.filestorageapi.dto.form.MetadataForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.exception.CircularFolderStructureException;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * Provides operations with folders.
 */
public interface FolderService {

    /**
     * Creates a new folder.
     *
     * @param metadataForm the metadata form.
     * @return the metadata of the created folder.
     * @throws RecordNotFoundException if the specified parent folder doesn't exist.
     */
    @PreAuthorize("hasPermission(#metadataForm.parentId, 'Folder', 'write')")
    Metadata createFolder(MetadataForm metadataForm);

    /**
     * Retrieves folder metadata.
     *
     * @param folderId the id of the folder.
     * @return the metadata.
     * @throws RecordNotFoundException if the specified folder doesn't exist.
     */
    @PreAuthorize("hasPermission(#folderId, 'Folder', 'read')")
    Metadata getFolderMetadata(Long folderId);

    /**
     * Retrieves folder contents.
     *
     * @param folderId the id of the folder.
     * @return the folder items.
     * @throws RecordNotFoundException if the specified folder doesn't exist.
     */
    @PreAuthorize("hasPermission(#folderId, 'Folder', 'read')")
    List<Metadata> getFolderItems(Long folderId);

    /**
     * Retrieves root folder metadata.
     *
     * @param ownerId the id of the owner.
     * @return the metadata.
     * @throws RecordNotFoundException if the specified owner doesn't exist.
     */
    @PreAuthorize("#ownerId eq principal.id")
    Metadata getRootFolderMetadata(Long ownerId);

    /**
     * Retrieves root folder contents.
     *
     * @param ownerId the id of the owner.
     * @return the items.
     * @throws RecordNotFoundException if the specified owner doesn't exist.
     */
    @PreAuthorize("#ownerId eq principal.id")
    List<Metadata> getRootFolderItems(Long ownerId);

    /**
     * Partially updates metadata of a folder. Allows to rename and/or move folders.
     *
     * @param folderId     the id of the folder.
     * @param metadataForm the metadata.
     * @return the updated metadata.
     * @throws RecordNotFoundException          if the specified folder or parent folder doesn't exist.
     * @throws CircularFolderStructureException if a circular reference occurs.
     */
    @PreAuthorize("(hasPermission(#folderId, 'Folder', 'edit') " +
            "and (#metadataForm.parentId eq null ? true : hasPermission(#metadataForm.parentId, 'Folder', 'write')))")
    Metadata updateFolderMetadata(Long folderId, MetadataForm metadataForm);

    /**
     * Deletes a folder.
     *
     * @param folderId  the id of the folder.
     * @param permanent delete permanently or put in the trash.
     * @throws RecordNotFoundException if the specified folder doesn't exist.
     */
    @PreAuthorize("hasPermission(#folderId, 'Folder', 'delete')")
    void deleteFolder(Long folderId, boolean permanent);

}
