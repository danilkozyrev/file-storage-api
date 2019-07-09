package com.github.danilkozyrev.filestorageapi;

import com.github.danilkozyrev.filestorageapi.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableTransactionManagement(order = 1000)
@EnableJpaRepositories(enableDefaultTransactions = false)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
