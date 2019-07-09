package com.github.danilkozyrev.filestorageapi.dto.projection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

@Data
public class Metadata {

    private Type type;
    private Long id;
    private String name;
    private Instant dateCreated;
    private Instant dateModified;
    private Long parentId;
    private Long ownerId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long size;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mimeType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean root;

    public enum Type {
        FILE, FOLDER
    }

}
