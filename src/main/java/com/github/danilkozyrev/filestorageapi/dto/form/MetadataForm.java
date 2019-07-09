package com.github.danilkozyrev.filestorageapi.dto.form;

import com.github.danilkozyrev.filestorageapi.validation.NullOrNotBlank;
import com.github.danilkozyrev.filestorageapi.validation.ValidationGroups;
import lombok.Data;

import javax.validation.constraints.*;

@Data
public class MetadataForm {

    @NotBlank(groups = ValidationGroups.Create.class)
    @NullOrNotBlank(groups = ValidationGroups.Update.class)
    @Size(min = 1, max = 255)
    private String name;

    @NotNull(groups = ValidationGroups.Create.class)
    @Positive
    private Long parentId;

}
