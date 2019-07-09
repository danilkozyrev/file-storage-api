package com.github.danilkozyrev.filestorageapi.security;

import com.github.danilkozyrev.filestorageapi.domain.File;
import com.github.danilkozyrev.filestorageapi.domain.Folder;
import com.github.danilkozyrev.filestorageapi.persistence.FileRepository;
import com.github.danilkozyrev.filestorageapi.persistence.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class StorageItemPermissionEvaluator implements PermissionEvaluator {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    /**
     * {@inheritDoc}
     *
     * @param targetDomainObject {@inheritDoc} Must be {@link File} or {@link Folder}.
     * @param permission         "write" to add items to folder, "edit" to edit metadata, "read" to read metadata or
     *                           contents, "delete" to delete an item.
     * @return false if the targetDomainObject is a root folder and the permission is "edit" or "delete". Otherwise
     * checks if the current user is the owner of the targetDomainObject.
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !(permission instanceof String)) {
            throw new IllegalArgumentException();
        } else if (targetDomainObject == null) {
            return false;
        }

        Long currentUserId = ((UserPrincipal) authentication.getPrincipal()).getId();
        if (targetDomainObject instanceof File) {
            File file = (File) targetDomainObject;
            return file.getOwner().getId().equals(currentUserId);
        } else if (targetDomainObject instanceof Folder) {
            Folder folder = (Folder) targetDomainObject;
            // Cannot modify root folder (only read and add items).
            if (folder.getRoot() && (permission.equals("edit") || permission.equals("delete"))) {
                return false;
            } else {
                return folder.getOwner().getId().equals(currentUserId);
            }
        } else {
            throw new IllegalArgumentException("Invalid target domain object " + targetDomainObject.getClass());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param targetType must be "File" or "Folder".
     * @param permission "write" to add items to folder, "edit" to edit metadata, "read" to read metadata or contents,
     *                   "delete" to delete an item.
     * @return result of {@link #hasPermission(Authentication, Object, Object)} method or false if the requested object
     * doesn't exist.
     */
    @Override
    public boolean hasPermission(
            Authentication authentication,
            Serializable targetId,
            String targetType,
            Object permission) {
        if (authentication == null || targetType == null || !(permission instanceof String)) {
            throw new IllegalArgumentException();
        } else if (targetId == null) {
            return false;
        } else if (!(targetId instanceof Long)) {
            throw new IllegalArgumentException("targetId is not numeric");
        }

        switch (targetType) {
            case "File":
                File file = fileRepository.findById((Long) targetId).orElse(null);
                return hasPermission(authentication, file, permission);
            case "Folder":
                Folder folder = folderRepository.findById((Long) targetId).orElse(null);
                return hasPermission(authentication, folder, permission);
            default:
                throw new IllegalArgumentException("Invalid target type " + targetType);
        }
    }

}
