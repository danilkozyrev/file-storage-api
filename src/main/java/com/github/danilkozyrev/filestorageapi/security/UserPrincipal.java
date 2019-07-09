package com.github.danilkozyrev.filestorageapi.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Extends the {@link User} class providing an additional 'id' field.
 */
@Getter
public class UserPrincipal extends User {

    private final Long id;

    public UserPrincipal(
            Long id,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }

}
