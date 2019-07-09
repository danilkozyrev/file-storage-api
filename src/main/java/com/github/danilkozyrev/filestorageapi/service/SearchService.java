package com.github.danilkozyrev.filestorageapi.service;

import com.github.danilkozyrev.filestorageapi.dto.form.SearchForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.Instant;
import java.util.List;

/**
 * Allows to search for files and folders.
 */
public interface SearchService {

    /**
     * Searches for files and folders using search parameters. Should return only files if any mime type is specified.
     *
     * @param searchForm the search parameters.
     * @param ownerId    the id of the owner.
     * @return the list of found files and folders.
     * @throws RecordNotFoundException RecordNotFoundException if the specified owner doesn't exist.
     */
    @PreAuthorize("#ownerId eq principal.id")
    List<Metadata> findItems(SearchForm searchForm, Long ownerId);

    /**
     * Retrieves a list of recently (in the last 7 days) modified files and folders.
     *
     * @param afterDate only items updated after this date will be returned.
     * @param ownerId   the id of the owner.
     * @return the list of recently modified files and folders.
     * @throws RecordNotFoundException RecordNotFoundException if the specified owner doesn't exist.
     */
    @PreAuthorize("#ownerId eq principal.id")
    List<Metadata> findRecentItems(Instant afterDate, Long ownerId);

}
