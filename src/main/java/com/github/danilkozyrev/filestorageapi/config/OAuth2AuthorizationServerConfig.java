package com.github.danilkozyrev.filestorageapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Bean
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()")
                .passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
                .inMemory()

                // Sample confidential client where client secret can be kept safe.
                .withClient("confidential")
                .secret(passwordEncoder.encode("confidential"))
                .authorizedGrantTypes("authorization_code", "refresh_token")
                .scopes("read", "write", "full")
                .redirectUris("https://localhost:8443/confidential")
                .accessTokenValiditySeconds(3600)
                .refreshTokenValiditySeconds(7 * 24 * 3600)

                .and()

                // Sample public client where client secret is vulnerable.
                .withClient("public")
                .authorizedGrantTypes("implicit")
                .scopes("read", "write", "full")
                .redirectUris("https://localhost:8443/public")
                .accessTokenValiditySeconds(3600)

                .and()

                // Swagger client.
                .withClient("swagger")
                .authorizedGrantTypes("implicit")
                .scopes("read", "write", "full")
                .redirectUris("https://localhost:8443/webjars/springfox-swagger-ui/oauth2-redirect.html")
                .accessTokenValiditySeconds(3600)
                .autoApprove(true);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .tokenStore(tokenStore())
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService);
    }

}
