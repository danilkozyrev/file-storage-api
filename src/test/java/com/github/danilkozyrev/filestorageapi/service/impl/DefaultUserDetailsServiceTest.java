package com.github.danilkozyrev.filestorageapi.service.impl;

import com.github.danilkozyrev.filestorageapi.domain.User;
import com.github.danilkozyrev.filestorageapi.persistence.UserRepository;
import com.github.danilkozyrev.filestorageapi.security.UserPrincipal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.danilkozyrev.filestorageapi.util.EntityBuilders.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private DefaultUserDetailsService userDetailsService;

    @Test
    public void loadUserByUsername_whenUserFound_shouldReturnUserPrincipal() {
        String email = "john.doe@example.com";
        User user = defaultUser()
                .email(email)
                .roles(Set.of(
                        defaultRole().id(0L).name("USER").build(),
                        defaultRole().id(1L).name("MANAGER").build()))
                .build();

        when(userRepository.findUserByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        UserPrincipal returnedUserPrincipal = userDetailsService.loadUserByUsername(email);
        List<String> returnedAuthorities = returnedUserPrincipal
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        assertThat(returnedUserPrincipal.getId(), equalTo(user.getId()));
        assertThat(returnedUserPrincipal.getUsername(), equalTo(user.getEmail()));
        assertThat(returnedUserPrincipal.getPassword(), equalTo(user.getPassword()));
        assertThat(returnedAuthorities, containsInAnyOrder("ROLE_USER", "ROLE_MANAGER"));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void loadUserByUsername_whenUserNotFound_shouldThrowException() {
        String email = "john.doe@example.com";
        when(userRepository.findUserByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        userDetailsService.loadUserByUsername(email);
    }

}
