package com.github.danilkozyrev.filestorageapi.mapper;

import com.github.danilkozyrev.filestorageapi.domain.*;
import com.github.danilkozyrev.filestorageapi.dto.form.SearchForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        implementationName = "DefaultItemProbeMapper",
        implementationPackage = "com.github.danilkozyrev.filestorageapi.mapper.impl")
public interface ItemProbeMapper {

    @Mapping(source = "searchForm.name", target = "name")
    @Mapping(source = "searchForm.parentId", target = "parent")
    @Mapping(source = "ownerId", target = "owner")
    Folder mapToFolderProbe(SearchForm searchForm, Long ownerId);

    @Mapping(source = "searchForm.name", target = "name")
    @Mapping(source = "searchForm.mimeType", target = "mimeType")
    @Mapping(source = "searchForm.parentId", target = "parent")
    @Mapping(source = "ownerId", target = "owner")
    File mapToFileProbe(SearchForm searchForm, Long ownerId);

    default User ownerIdToOwner(Long ownerId) {
        User owner = new User();
        owner.setId(ownerId);
        return owner;
    }

    default Folder parentIdToParent(Long parentId) {
        Folder parent = new Folder();
        parent.setId(parentId);
        return parent;
    }

}
