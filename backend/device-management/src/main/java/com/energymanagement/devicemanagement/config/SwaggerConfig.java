package com.energymanagement.devicemanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

 // http://localhost:8082/swagger-ui.html
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI deviceServiceAPI()
    {
        //  schema de securitate JWT
        String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                .info(new Info()
                        .title("Device Service API")
                        .description("API pentru managementul dispozitivelor")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Energy Management Team")
                                .email("support@energymanagement.com")))
                // Adauga suport pentru JWT token
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
