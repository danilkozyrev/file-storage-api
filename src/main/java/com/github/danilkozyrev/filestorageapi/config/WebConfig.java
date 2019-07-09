package com.github.danilkozyrev.filestorageapi.config;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Configuration
public class WebConfig {

    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
                Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);
                Object status = errorAttributes.get("status");
                if (status.equals(HttpStatus.INTERNAL_SERVER_ERROR.value())) {
                    errorAttributes.put("message", "The server has encountered an internal error");
                }
                return errorAttributes;
            }
        };
    }

}
