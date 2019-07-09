package com.github.danilkozyrev.filestorageapi.domain;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "folders", indexes = {@Index(columnList = "parent_id"), @Index(columnList = "owner_id")})
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class Folder extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean root;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<Folder> subfolders = new HashSet<>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<File> files = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

}
