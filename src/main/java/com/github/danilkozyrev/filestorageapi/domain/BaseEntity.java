package com.github.danilkozyrev.filestorageapi.domain;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
@Data
public abstract class BaseEntity implements Serializable {

    @Id
    @Column(updatable = false)
    @SequenceGenerator(name = "id_sequence_generator", initialValue = 1000)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_sequence_generator")
    @ToString.Include
    private Long id;

}
