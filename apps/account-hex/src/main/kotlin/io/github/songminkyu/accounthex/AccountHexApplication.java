package io.github.songminkyu.accounthex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for the Account Hexagonal Architecture application.
 * This class bootstraps the application and enables the necessary Spring Boot features.
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories
@EnableJpaAuditing
public class AccountHexApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountHexApplication.class, args);
    }
}