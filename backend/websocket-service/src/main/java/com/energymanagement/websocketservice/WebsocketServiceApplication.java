package com.energymanagement.websocketservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// The default in-memory user (and its generated password) is not needed: there is no login on this service
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class WebsocketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketServiceApplication.class, args);
    }
}