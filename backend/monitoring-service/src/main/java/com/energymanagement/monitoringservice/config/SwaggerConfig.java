package com.energymanagement.monitoringservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Available at /swagger-ui.html
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI monitoringServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Monitoring Service API")
                        .description("Hourly aggregation of device measurements and overconsumption detection")
                        .version("1.0"));
    }
}