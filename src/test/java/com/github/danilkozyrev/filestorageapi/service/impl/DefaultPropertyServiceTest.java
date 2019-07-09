package com.github.danilkozyrev.filestorageapi.service.impl;

import com.github.danilkozyrev.filestorageapi.domain.File;
import com.github.danilkozyrev.filestorageapi.domain.Property;
import com.github.danilkozyrev.filestorageapi.dto.form.PropertyForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.PropertyInfo;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import com.github.danilkozyrev.filestorageapi.mapper.PropertyInfoMapper;
import com.github.danilkozyrev.filestorageapi.persistence.FileRepository;
import com.github.danilkozyrev.filestorageapi.persistence.PropertyRepository;
import com.github.danilkozyrev.filestorageapi.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static com.github.danilkozyrev.filestorageapi.util.EntityBuilders.*;
import static com.github.danilkozyrev.filestorageapi.util.FormBuilders.*;
import static com.github.danilkozyrev.filestorageapi.util.ProjectionBuilders.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultPropertyServiceTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private FileRepository fileRepository;
    @Mock private PropertyInfoMapper propertyInfoMapper;

    @InjectMocks private DefaultPropertyService propertyService;

    @Captor private ArgumentCaptor<Property> savedPropertyCaptor;
    @Captor private ArgumentCaptor<Iterable<Property>> mappedPropertiesCaptor;

    @Test
    public void saveProperties_whenFileFoundAndPropertiesNotExist_shouldCreateProperties() {
        long fileId = 0L;
        File file = defaultFile().id(fileId).build();
        PropertyForm propertyForm = defaultPropertyForm().build();
        Property savedProperty = defaultProperty().build();
        List<PropertyInfo> propertyInfoList = List.of(defaultPropertyInfo().build());

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(propertyRepository.save(any())).thenReturn(savedProperty);
        when(propertyInfoMapper.mapProperties(any())).thenReturn(propertyInfoList);

        List<PropertyInfo> returnedPropertyInfoList = propertyService.saveProperties(fileId, Set.of(propertyForm));

        verify(propertyRepository).save(savedPropertyCaptor.capture());
        verify(propertyInfoMapper).mapProperties(mappedPropertiesCaptor.capture());

        Property createdProperty = savedPropertyCaptor.getValue();
        assertThat(createdProperty.getKey(), equalTo(propertyForm.getKey()));
        assertThat(createdProperty.getValue(), equalTo(propertyForm.getValue()));
        assertThat(createdProperty.getFile(), equalTo(file));

        assertThat(mappedPropertiesCaptor.getValue(), contains(savedProperty));
        assertThat(returnedPropertyInfoList, equalTo(propertyInfoList));
    }

    @Test
    public void saveProperties_whenFileFoundAndPropertiesExist_shouldUpdateProperties() {
        long fileId = 0L;
        Property fileProperty = defaultProperty().key("Key").value(null).build();
        File file = defaultFile().id(fileId).properties(Set.of(fileProperty)).build();
        PropertyForm propertyForm = defaultPropertyForm().key("Key").value("Value").build();
        List<PropertyInfo> propertyInfoList = List.of(defaultPropertyInfo().build());

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(propertyInfoMapper.mapProperties(any())).thenReturn(propertyInfoList);

        List<PropertyInfo> returnedPropertyInfoList = propertyService.saveProperties(fileId, Set.of(propertyForm));

        verify(fileRepository).findById(fileId);
        verifyNoMoreInteractions(fileRepository);
        verify(propertyInfoMapper).mapProperties(mappedPropertiesCaptor.capture());

        assertThat(fileProperty.getValue(), equalTo(propertyForm.getValue()));
        assertThat(mappedPropertiesCaptor.getValue(), contains(fileProperty));
        assertThat(returnedPropertyInfoList, equalTo(propertyInfoList));
    }

    @Test(expected = RecordNotFoundException.class)
    public void saveProperties_whenFileNotFound_shouldThrowException() {
        long fileId = 0L;
        PropertyForm propertyForm = defaultPropertyForm().build();

        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        propertyService.saveProperties(fileId, Set.of(propertyForm));
    }

    @Test
    public void getProperties_whenFileFound_shouldReturnPropertiesInfo() {
        long fileId = 0L;
        Property fileProperty = defaultProperty().build();
        File file = defaultFile().id(fileId).properties(Set.of(fileProperty)).build();
        List<PropertyInfo> propertyInfoList = List.of(ProjectionBuilders.defaultPropertyInfo().build());

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(propertyInfoMapper.mapProperties(file.getProperties())).thenReturn(propertyInfoList);

        List<PropertyInfo> returnedPropertyInfoList = propertyService.getProperties(fileId);

        assertThat(returnedPropertyInfoList, equalTo(propertyInfoList));
    }

    @Test(expected = RecordNotFoundException.class)
    public void getProperties_whenFileNotFound_shouldThrowException() {
        long fileId = 0L;
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());
        propertyService.getProperties(fileId);
    }

    @Test
    public void deleteProperties_whenFileFound_shouldDeleteProperties() {
        long fileId = 0L;
        Property fileProperty = defaultProperty().build();
        File file = defaultFile().id(fileId).properties(Set.of(fileProperty)).build();

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        propertyService.deleteProperties(fileId);

        verify(propertyRepository).deleteInBatch(file.getProperties());
    }

    @Test(expected = RecordNotFoundException.class)
    public void deleteProperties_whenFileNotFound_shouldThrowException() {
        long fileId = 0L;
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());
        propertyService.deleteProperties(fileId);
    }

}
