package com.github.danilkozyrev.filestorageapi.mapper;

import com.github.danilkozyrev.filestorageapi.domain.Property;
import com.github.danilkozyrev.filestorageapi.dto.projection.PropertyInfo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        implementationName = "DefaultPropertyInfoMapper",
        implementationPackage = "com.github.danilkozyrev.filestorageapi.mapper.impl")
public interface PropertyInfoMapper {

    PropertyInfo mapProperty(Property property);

    List<PropertyInfo> mapProperties(Iterable<Property> properties);

}
