package com.github.danilkozyrev.filestorageapi.service;

import com.github.danilkozyrev.filestorageapi.dto.form.UserForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.UserInfo;
import com.github.danilkozyrev.filestorageapi.exception.EmailAlreadyExistsException;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * Provides operations with users.
 */
public interface UserService {

    /**
     * Creates a new user.
     *
     * @param userForm the user form.
     * @return the information about the created user.
     * @throws EmailAlreadyExistsException if the specified email already exists.
     */
    @PreAuthorize("hasRole('MANAGER')")
    UserInfo createUser(UserForm userForm);

    /**
     * Retrieves information about all users of the application.
     *
     * @return all users of the application.
     */
    @PreAuthorize("hasRole('MANAGER')")
    List<UserInfo> getAllUsers();

    /**
     * Retrieves information about a user by id.
     *
     * @param userId the id of the user.
     * @return the information.
     * @throws RecordNotFoundException if the specified user doesn't exist.
     */
    @PreAuthorize("hasRole('MANAGER') or #userId eq principal.id")
    UserInfo getUserInfo(Long userId);

    /**
     * Partially updates information about a user by id.
     *
     * @param userId   the id of the user.
     * @param userForm the user form.
     * @return the updated information.
     * @throws RecordNotFoundException     if the specified user doesn't exist.
     * @throws EmailAlreadyExistsException if the specified email already exists.
     */
    @PreAuthorize("hasRole('MANAGER')")
    UserInfo updateUserInfo(Long userId, UserForm userForm);

    /**
     * Deletes a user by id.
     *
     * @param userId the id of the user.
     * @throws RecordNotFoundException if the specified user doesn't exist.
     */
    @PreAuthorize("hasRole('MANAGER')")
    void deleteUser(Long userId);

}
