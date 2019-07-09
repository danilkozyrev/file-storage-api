package com.github.danilkozyrev.filestorageapi.config;

import com.github.danilkozyrev.filestorageapi.security.UserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.github.danilkozyrev.filestorageapi.web"))
                .paths(PathSelectors.ant("/api/**"))
                .build()
                .ignoredParameterTypes(UriComponentsBuilder.class, UserPrincipal.class, Resource.class)
                .tags(
                        new Tag("Files", "Provides operations with files"),
                        new Tag("Folders", "Provides operations with folders"),
                        new Tag("Properties", "Properties allow custom metadata to be set on files"),
                        new Tag("Search", "Allows to findItems for files and folders of the current user"),
                        new Tag("Trash", "Provides operations with the current user's trash"),
                        new Tag("Users", "Provides operations with users"))
                .useDefaultResponseMessages(false)
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts())
                .apiInfo(apiInfo());
    }

    @Bean
    public SecurityConfiguration securityConfiguration() {
        return SecurityConfigurationBuilder
                .builder()
                .clientId("swagger")
                .scopeSeparator(" ")
                .build();
    }

    private List<GrantType> grantTypes() {
        ImplicitGrant implicitGrant = new ImplicitGrantBuilder()
                .loginEndpoint(new LoginEndpoint("/oauth/authorize"))
                .tokenName("Access token")
                .build();
        return List.of(implicitGrant);
    }

    private List<AuthorizationScope> scopes() {
        return List.of(
                new AuthorizationScope("read", "Allows read operations"),
                new AuthorizationScope("write", "Allows write operations"),
                new AuthorizationScope("full", "Allows all operations"));
    }

    private List<SecurityScheme> securitySchemes() {
        OAuth oAuth = new OAuthBuilder()
                .name("OAuth2")
                .grantTypes(grantTypes())
                .scopes(scopes())
                .build();
        return List.of(oAuth);
    }

    private List<SecurityContext> securityContexts() {
        AuthorizationScope[] scopeArray = scopes().toArray(AuthorizationScope[]::new);
        SecurityContext securityContext = SecurityContext
                .builder()
                .securityReferences(List.of(new SecurityReference("OAuth2", scopeArray)))
                .forPaths(PathSelectors.ant("/api/**"))
                .build();
        return List.of(securityContext);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("File Storage API")
                .description("This application allows you to store files in Dropbox-like manner")
                .version("1.0")
                .contact(new Contact("Danil Kozyrev", "http://github.com/danilkozyrev", "kozyrev.danil@gmail.com"))
                .license("MIT License")
                .licenseUrl("https://opensource.org/licenses/MIT")
                .build();
    }

}
