package com.github.danilkozyrev.filestorageapi.dto.form;

import lombok.Data;
import lombok.experimental.Delegate;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

@Data
public class ValidSet<T> implements Set<T> {

    @Valid
    @NotEmpty
    @Delegate
    private Set<T> internal = new HashSet<>();

}
