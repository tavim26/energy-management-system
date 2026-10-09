package com.energymanagement.authorizationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Available at /swagger-ui.html
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI authServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Authorization Service API")
                        .description("Registration, login and JWT generation")
                        .version("1.0"));
    }
}