package com.github.danilkozyrev.filestorageapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application-specific configuration properties.
 */
@ConfigurationProperties("application")
@Getter
@Setter
public class ApplicationProperties {

    /**
     * Base folder for uploaded files.
     */
    private String baseFolder;

    /**
     * Base storage limit in bytes.
     */
    private Long baseLimit;

    /**
     * Encryption key for signing file tokens.
     */
    private String fileTokenSecret;

    /**
     * File token validity time in seconds.
     */
    private Long fileTokenValidity;

}
