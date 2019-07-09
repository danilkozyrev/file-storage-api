package com.github.danilkozyrev.filestorageapi.service.impl;

import com.github.danilkozyrev.filestorageapi.domain.Folder;
import com.github.danilkozyrev.filestorageapi.domain.User;
import com.github.danilkozyrev.filestorageapi.dto.form.UserForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.UserInfo;
import com.github.danilkozyrev.filestorageapi.event.EntityDeletionEvent;
import com.github.danilkozyrev.filestorageapi.exception.EmailAlreadyExistsException;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import com.github.danilkozyrev.filestorageapi.mapper.UserInfoMapper;
import com.github.danilkozyrev.filestorageapi.persistence.RoleRepository;
import com.github.danilkozyrev.filestorageapi.persistence.UserRepository;
import com.github.danilkozyrev.filestorageapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserInfoMapper userInfoMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public UserInfo createUser(UserForm userForm) {
        assertEmailIsUnique(userForm.getEmail());

        User user = new User();
        user.setEmail(userForm.getEmail());
        user.setPassword(passwordEncoder.encode(userForm.getPassword()));
        user.setFirstName(userForm.getFirstName());
        user.setLastName(userForm.getLastName());
        user.getRoles().add(roleRepository.getUserRole());

        // Create a root folder for the user.
        Folder root = new Folder();
        root.setName(UUID.randomUUID().toString());
        root.setRoot(true);
        root.setOwner(user);
        user.getFolders().add(root);

        user = userRepository.save(user);
        return userInfoMapper.mapUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserInfo> getAllUsers() {
        List<User> users = userRepository.findAllJoinRoles();
        return userInfoMapper.mapUsers(users);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfo getUserInfo(Long userId) {
        User user = getUser(userId);
        return userInfoMapper.mapUser(user);
    }

    @Override
    @Transactional
    public UserInfo updateUserInfo(Long userId, UserForm userForm) {
        User user = getUser(userId);
        if (userForm.getEmail() != null) {
            assertEmailIsUnique(userForm.getEmail());
            user.setEmail(userForm.getEmail());
        }
        if (userForm.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(userForm.getPassword());
            user.setPassword(encodedPassword);
        }
        if (userForm.getFirstName() != null) {
            user.setFirstName(userForm.getFirstName());
        }
        if (userForm.getLastName() != null) {
            user.setLastName(userForm.getLastName());
        }
        return userInfoMapper.mapUser(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUser(userId);
        eventPublisher.publishEvent(new EntityDeletionEvent<>(this, user.getFiles()));
        userRepository.delete(user);
    }

    private void assertEmailIsUnique(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException("The email " + email + " is already in use");
        }
    }

    private User getUser(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new RecordNotFoundException(User.class, userId));
    }

}
