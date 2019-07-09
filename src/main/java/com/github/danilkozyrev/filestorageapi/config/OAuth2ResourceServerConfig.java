package com.github.danilkozyrev.filestorageapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@EnableResourceServer
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                .antMatchers("/api/**")
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/users/**")
                .access("#oauth2.hasAnyScope('read', 'full')")
                .antMatchers("/api/users/**")
                .access("#oauth2.hasScope('full')")
                .antMatchers(HttpMethod.GET)
                .access("#oauth2.hasAnyScope('read', 'full')")
                .anyRequest()
                .access("#oauth2.hasAnyScope('write', 'full')");
    }

}
