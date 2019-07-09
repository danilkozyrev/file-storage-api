package com.github.danilkozyrev.filestorageapi.service;

import com.github.danilkozyrev.filestorageapi.dto.form.PropertyForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.PropertyInfo;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Set;

/**
 * Provides operations with file properties.
 */
public interface PropertyService {

    /**
     * Saves properties. If a property with a given key exists then it's value is updated, otherwise creates a new
     * property.
     *
     * @param fileId          the id of the file.
     * @param propertyFormSet the properties to save.
     * @return the properties belonging to the file.
     * @throws RecordNotFoundException if the specified file doesn't exist.
     */
    @PreAuthorize("hasPermission(#fileId, 'File', 'edit')")
    List<PropertyInfo> saveProperties(Long fileId, Set<PropertyForm> propertyFormSet);

    /**
     * Retrieves all properties for a given file.
     *
     * @param fileId the id of the file.
     * @return the properties belonging to the file.
     * @throws RecordNotFoundException if the specified file doesn't exist.
     */
    @PreAuthorize("hasPermission(#fileId, 'File', 'read')")
    List<PropertyInfo> getProperties(Long fileId);

    /**
     * Deletes all properties for a given file.
     *
     * @param fileId the id of the file.
     * @throws RecordNotFoundException if the specified file doesn't exist.
     */
    @PreAuthorize("hasPermission(#fileId, 'File', 'edit')")
    void deleteProperties(Long fileId);

}
