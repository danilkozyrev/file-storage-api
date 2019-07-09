package com.github.danilkozyrev.filestorageapi.dto.projection;

import lombok.Data;

import java.time.Instant;

@Data
public class PropertyInfo {

    private String key;
    private String value;
    private Instant dateCreated;
    private Instant dateModified;

}
