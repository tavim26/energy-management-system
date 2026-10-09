package com.energymanagement.authorizationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Logins are checked by AuthService against the credentials table, so Spring's
// default in-memory user (and the generated password it logs) is not needed
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class AuthorizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorizationServiceApplication.class, args);
    }
}