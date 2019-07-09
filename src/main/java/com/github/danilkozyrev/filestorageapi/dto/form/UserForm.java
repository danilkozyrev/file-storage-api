package com.github.danilkozyrev.filestorageapi.dto.form;

import com.github.danilkozyrev.filestorageapi.validation.*;
import lombok.Data;

import javax.validation.constraints.*;

@Data
public class UserForm {

    @NotBlank(groups = ValidationGroups.Create.class)
    @NullOrNotBlank(groups = ValidationGroups.Update.class)
    @Email
    private String email;

    @NotNull(groups = ValidationGroups.Create.class)
    @ValidPassword
    private String password;

    @NotBlank(groups = ValidationGroups.Create.class)
    @NullOrNotBlank(groups = ValidationGroups.Update.class)
    @Size(min = 1, max = 255)
    private String firstName;

    @NotBlank(groups = ValidationGroups.Create.class)
    @NullOrNotBlank(groups = ValidationGroups.Update.class)
    @Size(min = 1, max = 255)
    private String lastName;

}
