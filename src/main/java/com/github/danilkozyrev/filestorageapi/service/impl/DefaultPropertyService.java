package com.github.danilkozyrev.filestorageapi.service.impl;

import com.github.danilkozyrev.filestorageapi.domain.File;
import com.github.danilkozyrev.filestorageapi.domain.Property;
import com.github.danilkozyrev.filestorageapi.dto.form.PropertyForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.PropertyInfo;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import com.github.danilkozyrev.filestorageapi.mapper.PropertyInfoMapper;
import com.github.danilkozyrev.filestorageapi.persistence.FileRepository;
import com.github.danilkozyrev.filestorageapi.persistence.PropertyRepository;
import com.github.danilkozyrev.filestorageapi.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultPropertyService implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final FileRepository fileRepository;
    private final PropertyInfoMapper propertyInfoMapper;

    @Override
    @Transactional
    public List<PropertyInfo> saveProperties(Long fileId, Set<PropertyForm> propertyFormSet) {
        File file = getFile(fileId);
        Map<String, Property> existingProperties = extractPropertiesAsMap(file);
        for (PropertyForm propertyForm : propertyFormSet) {
            String key = propertyForm.getKey();
            String value = propertyForm.getValue();
            if (existingProperties.containsKey(key)) {
                // Update value if the key exists.
                existingProperties.get(key).setValue(value);
            } else {
                // Create a new property otherwise.
                Property newProperty = createAndSaveProperty(key, value, file);
                existingProperties.put(key, newProperty);
            }
        }
        return propertyInfoMapper.mapProperties(existingProperties.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyInfo> getProperties(Long fileId) {
        Set<Property> properties = getFile(fileId).getProperties();
        return propertyInfoMapper.mapProperties(properties);
    }

    @Override
    @Transactional
    public void deleteProperties(Long fileId) {
        Set<Property> properties = getFile(fileId).getProperties();
        propertyRepository.deleteInBatch(properties);
    }

    private File getFile(Long fileId) {
        return fileRepository
                .findById(fileId)
                .orElseThrow(() -> new RecordNotFoundException(File.class, fileId));
    }

    private Map<String, Property> extractPropertiesAsMap(File file) {
        return file
                .getProperties()
                .stream()
                .collect(Collectors.toMap(Property::getKey, Function.identity()));
    }

    private Property createAndSaveProperty(String key, String value, File file) {
        Property property = new Property();
        property.setKey(key);
        property.setValue(value);
        property.setFile(file);
        return propertyRepository.save(property);
    }

}
