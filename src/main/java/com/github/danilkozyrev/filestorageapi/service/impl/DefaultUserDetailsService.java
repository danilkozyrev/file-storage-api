package com.github.danilkozyrev.filestorageapi.service.impl;

import com.github.danilkozyrev.filestorageapi.domain.User;
import com.github.danilkozyrev.filestorageapi.persistence.UserRepository;
import com.github.danilkozyrev.filestorageapi.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DefaultUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserPrincipal loadUserByUsername(String email) {
        User user = userRepository
                .findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("The email " + email + " has not been found"));
        List<SimpleGrantedAuthority> grantedAuthorities = user
                .getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), grantedAuthorities);
    }

}
