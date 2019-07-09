package com.github.danilkozyrev.filestorageapi.domain;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class Role extends BaseEntity {

    @Column(nullable = false, updatable = false)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

}
