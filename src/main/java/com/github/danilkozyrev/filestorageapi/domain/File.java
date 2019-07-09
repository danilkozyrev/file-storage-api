package com.github.danilkozyrev.filestorageapi.domain;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "files",
        uniqueConstraints = @UniqueConstraint(columnNames = "location"),
        indexes = {@Index(columnList = "parent_id"), @Index(columnList = "owner_id")})
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class File extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Property> properties = new HashSet<>();

}
