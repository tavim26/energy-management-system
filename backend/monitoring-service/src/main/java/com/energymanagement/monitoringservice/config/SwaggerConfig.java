package com.energymanagement.monitoringservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


// http://localhost:8084/swagger-ui/index.html

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI monitoringServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Monitoring Service API")
                        .description("Energy Management System - Monitoring Microservice")
                        .version("1.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("Local server")));
    }
}