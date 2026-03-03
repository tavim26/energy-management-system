package com.energymanagement.websocketservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// ADAUGĂ LINIA DE EXCLUDERE AICI!
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class WebsocketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketServiceApplication.class, args);
    }
}