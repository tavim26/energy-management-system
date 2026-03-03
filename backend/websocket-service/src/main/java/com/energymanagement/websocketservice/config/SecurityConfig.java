package com.energymanagement.websocketservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
                // dezactivare CSRF (nu e necesar pentru acest tip de arhitectura stateless/websocket)
                .csrf(csrf -> csrf.disable())

                // reguli autorizare
                .authorizeHttpRequests(auth -> auth
                        // permitere acces public pentru websocket handshake
                        .requestMatchers("/ws/**").permitAll()
                        // permitere healthcheck
                        .requestMatchers("/api/websocket/health").permitAll()
                        // restul necesita autnetificare
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}