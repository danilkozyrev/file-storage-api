package com.github.danilkozyrev.filestorageapi.dto.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class PropertyForm {

    @NotBlank
    @Size(min = 1, max = 255)
    private String key;

    @NotBlank
    @Size(min = 1, max = 255)
    private String value;

}
