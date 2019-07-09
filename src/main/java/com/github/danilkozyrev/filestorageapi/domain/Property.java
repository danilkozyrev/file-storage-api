package com.github.danilkozyrev.filestorageapi.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(
        name = "properties",
        uniqueConstraints = @UniqueConstraint(columnNames = {"key", "file_id"}),
        indexes = @Index(columnList = "file_id"))
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class Property extends AuditableEntity {

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

}
