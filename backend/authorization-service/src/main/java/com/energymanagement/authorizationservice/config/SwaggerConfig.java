package com.energymanagement.authorizationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

 // http://localhost:8083/swagger-ui.html
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI authServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Authorization Service API")
                        .description("API pentru autentificare si autorizare utilizatori")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Energy Management Team")
                                .email("support@energymanagement.com")));
    }
}