package com.github.danilkozyrev.filestorageapi.dto.projection;

import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
public class UserInfo {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Instant dateRegistered;
    private Instant dateModified;
    private Set<String> roles = new HashSet<>();

}
