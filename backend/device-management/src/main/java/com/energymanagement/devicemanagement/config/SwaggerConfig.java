package com.energymanagement.devicemanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Available at /swagger-ui.html
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME = "Bearer Authentication";

    @Bean
    public OpenAPI deviceServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Device Service API")
                        .description("Device management and device-to-user assignment")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME, new SecurityScheme()
                                .name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}