package com.github.danilkozyrev.filestorageapi.service.impl;

import com.github.danilkozyrev.filestorageapi.domain.*;
import com.github.danilkozyrev.filestorageapi.dto.form.UserForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.UserInfo;
import com.github.danilkozyrev.filestorageapi.event.EntityDeletionEvent;
import com.github.danilkozyrev.filestorageapi.exception.EmailAlreadyExistsException;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import com.github.danilkozyrev.filestorageapi.mapper.UserInfoMapper;
import com.github.danilkozyrev.filestorageapi.persistence.RoleRepository;
import com.github.danilkozyrev.filestorageapi.persistence.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static com.github.danilkozyrev.filestorageapi.util.EntityBuilders.*;
import static com.github.danilkozyrev.filestorageapi.util.FormBuilders.*;
import static com.github.danilkozyrev.filestorageapi.util.ProjectionBuilders.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserInfoMapper userInfoMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private DefaultUserService userService;

    @Captor private ArgumentCaptor<User> userCaptor;
    @Captor private ArgumentCaptor<EntityDeletionEvent<File>> fileDeletionEventCaptor;

    @Test
    public void createUser_whenEmailIsUnique_shouldCreateUser() {
        UserForm userForm = defaultUserForm().build();
        Role userRole = defaultRole().build();
        String encodedPassword = new StringBuilder(userForm.getPassword()).reverse().toString();
        User savedUser = defaultUser().build();
        UserInfo userInfo = defaultUserInfo().build();

        when(userRepository.existsByEmailIgnoreCase(userForm.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userForm.getPassword())).thenReturn(encodedPassword);
        when(roleRepository.getUserRole()).thenReturn(userRole);
        when(userRepository.save(any())).thenReturn(savedUser);
        when(userInfoMapper.mapUser(savedUser)).thenReturn(userInfo);

        UserInfo returnedUserInfo = userService.createUser(userForm);

        verify(userRepository).save(userCaptor.capture());

        User createdUser = userCaptor.getValue();
        assertThat(createdUser.getEmail(), equalTo(userForm.getEmail()));
        assertThat(createdUser.getPassword(), equalTo(encodedPassword));
        assertThat(createdUser.getFirstName(), equalTo(userForm.getFirstName()));
        assertThat(createdUser.getLastName(), equalTo(userForm.getLastName()));
        assertThat(createdUser.getRoles(), contains(userRole));

        Folder createdUserRoot = createdUser.getFolders().stream().findAny().orElseThrow();
        assertThat(createdUserRoot.getName(), is(notNullValue()));
        assertThat(createdUserRoot.getRoot(), equalTo(true));
        assertThat(createdUserRoot.getParent(), is(nullValue()));
        assertThat(createdUserRoot.getOwner(), equalTo(createdUser));

        assertThat(returnedUserInfo, equalTo(userInfo));
    }

    @Test(expected = EmailAlreadyExistsException.class)
    public void createUser_whenEmailIsNotUnique_shouldThrowException() {
        UserForm userForm = defaultUserForm().build();
        when(userRepository.existsByEmailIgnoreCase(userForm.getEmail())).thenReturn(true);
        userService.createUser(userForm);
    }

    @Test
    public void getAllUsers_shouldReturnAllUsersInfo() {
        List<User> users = List.of(
                defaultUser().id(0L).build(),
                defaultUser().id(1L).build());
        List<UserInfo> userInfoList = List.of(
                defaultUserInfo().id(2L).build(),
                defaultUserInfo().id(3L).build());

        when(userRepository.findAllJoinRoles()).thenReturn(users);
        when(userInfoMapper.mapUsers(users)).thenReturn(userInfoList);

        List<UserInfo> returnedUserInfoList = userService.getAllUsers();

        assertThat(returnedUserInfoList, equalTo(userInfoList));
    }

    @Test
    public void getUserInfo_whenUserFound_shouldReturnUserInfo() {
        long userId = 0L;
        User user = defaultUser().id(userId).build();
        UserInfo userInfo = defaultUserInfo().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userInfoMapper.mapUser(user)).thenReturn(userInfo);

        UserInfo returnedUserInfo = userService.getUserInfo(userId);

        assertThat(returnedUserInfo, equalTo(userInfo));
    }

    @Test(expected = RecordNotFoundException.class)
    public void getUserInfo_whenUserNotFound_shouldThrowException() {
        long userId = 0L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        userService.getUserInfo(userId);
    }

    @Test
    public void updateUserInfo_whenUserFoundAndEmailIsUnique_shouldUpdateUser() {
        long userId = 0L;
        User user = defaultUser().id(userId).build();
        UserForm userForm = defaultUserForm().build();
        String encodedPassword = new StringBuilder(userForm.getPassword()).reverse().toString();
        UserInfo userInfo = defaultUserInfo().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCase(userForm.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userForm.getPassword())).thenReturn(encodedPassword);
        when(userInfoMapper.mapUser(user)).thenReturn(userInfo);

        UserInfo returnedUserInfo = userService.updateUserInfo(userId, userForm);

        assertThat(user.getFirstName(), equalTo(userForm.getFirstName()));
        assertThat(user.getLastName(), equalTo(userForm.getLastName()));
        assertThat(user.getEmail(), equalTo(userForm.getEmail()));
        assertThat(user.getPassword(), equalTo(encodedPassword));

        assertThat(returnedUserInfo, equalTo(userInfo));
    }

    @Test(expected = RecordNotFoundException.class)
    public void updateUserInfo_whenUserNotFound_shouldThrowException() {
        long userId = 0L;
        UserForm userForm = defaultUserForm().build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        userService.updateUserInfo(userId, userForm);
    }

    @Test(expected = EmailAlreadyExistsException.class)
    public void updateUserInfo_whenEmailIsNotUnique_shouldThrowException() {
        long userId = 0L;
        User user = defaultUser().id(userId).build();
        UserForm userForm = defaultUserForm().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCase(userForm.getEmail())).thenReturn(true);

        userService.updateUserInfo(userId, userForm);
    }

    @Test
    public void deleteUser_whenUserFound_shouldDeleteUserAndFiles() {
        long userId = 0L;
        User user = defaultUser()
                .id(userId)
                .files(Set.of(
                        defaultFile().id(1L).build(),
                        defaultFile().id(2L).build()))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(eventPublisher).publishEvent(fileDeletionEventCaptor.capture());
        verify(userRepository).delete(user);

        Iterable<File> deletedFiles = fileDeletionEventCaptor.getValue().getDeletedEntities();
        assertThat(deletedFiles, equalTo(user.getFiles()));
    }

    @Test(expected = RecordNotFoundException.class)
    public void deleteUser_whenUserNotFound_shouldThrowException() {
        long userId = 0L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        userService.deleteUser(userId);
    }

}
