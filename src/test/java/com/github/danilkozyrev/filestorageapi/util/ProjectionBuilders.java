package com.github.danilkozyrev.filestorageapi.util;

import com.github.danilkozyrev.filestorageapi.dto.projection.*;
import lombok.Builder;
import org.apache.tika.mime.MimeTypes;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public final class ProjectionBuilders {

    private ProjectionBuilders() {
    }

    public static class MetadataBuilder {
        private Metadata.Type type = Metadata.Type.FILE;
        private Long id = 0L;
        private String name = "File";
        private Long parentId = 0L;
        private Long ownerId = 0L;
        private Long size = 32L;
        private String mimeType = MimeTypes.OCTET_STREAM;
    }

    @Builder(builderMethodName = "defaultMetadata")
    private static Metadata createMetadata(
            Metadata.Type type, Long id, String name, Long parentId, Long ownerId, Long size, String mimeType,
            Boolean root) {
        Metadata metadata = new Metadata();
        metadata.setType(type);
        metadata.setId(id);
        metadata.setDateCreated(Instant.EPOCH);
        metadata.setDateModified(Instant.EPOCH);
        metadata.setName(name);
        metadata.setParentId(parentId);
        metadata.setOwnerId(ownerId);
        metadata.setSize(size);
        metadata.setMimeType(mimeType);
        metadata.setRoot(root);
        return metadata;
    }

    public static class PropertyInfoBuilder {
        private String key = "Key";
        private String value = "Value";
    }

    @Builder(builderMethodName = "defaultPropertyInfo")
    private static PropertyInfo createPropertyInfo(String key, String value) {
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.setDateCreated(Instant.EPOCH);
        propertyInfo.setDateModified(Instant.EPOCH);
        propertyInfo.setKey(key);
        propertyInfo.setValue(value);
        return propertyInfo;
    }

    public static class UserInfoBuilder {
        private Long id = 0L;
        private String firstName = "John";
        private String lastName = "Doe";
        private String email = "john.doe@Example.com";
        private Set<String> roles = new HashSet<>();
    }

    @Builder(builderMethodName = "defaultUserInfo")
    private static UserInfo createUserInfo(
            Long id, String firstName, String lastName, String email, Set<String> roles) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setDateRegistered(Instant.EPOCH);
        userInfo.setDateModified(Instant.EPOCH);
        userInfo.setFirstName(firstName);
        userInfo.setLastName(lastName);
        userInfo.setEmail(email);
        userInfo.setRoles(roles);
        return userInfo;
    }

}
