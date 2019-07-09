package com.github.danilkozyrev.filestorageapi.dto.form;

import com.github.danilkozyrev.filestorageapi.validation.NullOrNotBlank;
import lombok.Data;

import javax.validation.constraints.Positive;

@Data
public class SearchForm {

    @NullOrNotBlank
    private String name;

    @NullOrNotBlank
    private String mimeType;

    @Positive
    private Long parentId;

}
