package com.github.danilkozyrev.filestorageapi.util;

import com.github.danilkozyrev.filestorageapi.domain.*;
import lombok.Builder;
import org.apache.tika.mime.MimeTypes;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public final class EntityBuilders {

    private EntityBuilders() {
    }

    public static class FolderBuilder {
        private Long id = 0L;
        private String name = "Folder";
        private Boolean root = false;
        private Folder parent = new Folder();
        private User owner = new User();
        private Set<Folder> subFolders = new HashSet<>();
        private Set<File> files = new HashSet<>();
    }

    @Builder(builderMethodName = "defaultFolder")
    private static Folder createFolder(
            Long id, String name, Boolean root, Folder parent, User owner, Set<Folder> subFolders, Set<File> files) {
        Folder folder = new Folder();
        folder.setId(id);
        folder.setVersion(0L);
        folder.setDateCreated(Instant.EPOCH);
        folder.setDateModified(Instant.EPOCH);
        folder.setName(name);
        folder.setRoot(root);
        folder.setParent(parent);
        folder.setOwner(owner);
        folder.setSubfolders(subFolders);
        folder.setFiles(files);
        return folder;
    }

    public static class FileBuilder {
        private Long id = 0L;
        private String name = "Folder";
        private Long size = 32L;
        private String mimeType = MimeTypes.OCTET_STREAM;
        private String location = "/tmpdir";
        private Folder parent = new Folder();
        private User owner = new User();
        private Set<Property> properties = new HashSet<>();
    }

    @Builder(builderMethodName = "defaultFile")
    private static File createFile(
            Long id, String name, Long size, String mimeType, String location, Folder parent, User owner,
            Set<Property> properties) {
        File file = new File();
        file.setId(id);
        file.setVersion(0L);
        file.setDateCreated(Instant.EPOCH);
        file.setDateModified(Instant.EPOCH);
        file.setName(name);
        file.setSize(size);
        file.setMimeType(mimeType);
        file.setLocation(location);
        file.setParent(parent);
        file.setOwner(owner);
        file.setProperties(properties);
        return file;
    }

    public static class PropertyBuilder {
        private Long id = 0L;
        private String key = "Key";
        private String value = "Value";
        private File file = new File();
    }

    @Builder(builderMethodName = "defaultProperty")
    private static Property createProperty(Long id, String key, String value, File file) {
        Property property = new Property();
        property.setId(id);
        property.setVersion(0L);
        property.setDateCreated(Instant.EPOCH);
        property.setDateModified(Instant.EPOCH);
        property.setKey(key);
        property.setValue(value);
        property.setFile(file);
        return property;
    }

    public static class UserBuilder {
        private Long id = 0L;
        private String email = "john.doe@example.com";
        private String password = "john.doe";
        private String firstName = "John";
        private String lastName = "Doe";
        private Set<File> files = new HashSet<>();
        private Set<Folder> folders = new HashSet<>();
        private Set<Role> roles = new HashSet<>();
    }

    @Builder(builderMethodName = "defaultUser")
    private static User createUser(
            Long id, String email, String password, String firstName, String lastName, Set<File> files,
            Set<Folder> folders, Set<Role> roles) {
        User user = new User();
        user.setId(id);
        user.setVersion(0L);
        user.setDateCreated(Instant.EPOCH);
        user.setDateModified(Instant.EPOCH);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setFiles(files);
        user.setFolders(folders);
        user.setRoles(roles);
        return user;
    }

    public static class RoleBuilder {
        private Long id = 0L;
        private String name = "USER";
        private Set<User> users = new HashSet<>();
    }

    @Builder(builderMethodName = "defaultRole")
    private static Role createRole(Long id, String name, Set<User> users) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setUsers(users);
        return role;
    }

}
