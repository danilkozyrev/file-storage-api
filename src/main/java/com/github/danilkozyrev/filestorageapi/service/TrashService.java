package com.github.danilkozyrev.filestorageapi.service;

import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.exception.FileSystemException;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * Provides operations with a trash. Move file or folder to restore it.
 */
public interface TrashService {

    /**
     * Retrieves trash contents.
     *
     * @param ownerId the id of the owner.
     * @return the trash items.
     * @throws RecordNotFoundException RecordNotFoundException if the specified owner doesn't exist.
     */
    @PreAuthorize("#ownerId eq principal.id")
    List<Metadata> getTrashItems(Long ownerId);

    /**
     * Empties a trash.
     *
     * @param ownerId the id of the owner.
     * @throws FileSystemException     if the files can't be deleted.
     * @throws RecordNotFoundException RecordNotFoundException if the specified owner doesn't exist.
     */
    @PreAuthorize("#ownerId eq principal.id")
    void emptyTrash(Long ownerId);

}
