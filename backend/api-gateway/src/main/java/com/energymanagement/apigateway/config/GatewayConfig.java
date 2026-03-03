package com.energymanagement.apigateway.config;

import com.energymanagement.apigateway.filter.AuthenticationFilter;
import com.energymanagement.apigateway.filter.AuthorizationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig
{

    private final AuthenticationFilter authFilter;
    private final AuthorizationFilter authorizationFilter;

    @Value("${service.auth.url}")
    private String authServiceUrl;

    @Value("${service.user.url}")
    private String userServiceUrl;

    @Value("${service.device.url}")
    private String deviceServiceUrl;

    @Value("${service.support.url}")
    private String supportServiceUrl;

    @Value("${service.websocket.url}")
    private String websocketServiceUrl;

    public GatewayConfig(AuthenticationFilter authFilter, AuthorizationFilter authorizationFilter)
    {
        this.authFilter = authFilter;
        this.authorizationFilter = authorizationFilter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder)
    {
        return builder.routes()
                // Rute publice (fara autentificare - register si login)
                .route("auth-login", r -> r.path("/api/auth/login")
                        .uri(authServiceUrl))

                .route("auth-register", r -> r.path("/api/auth/register")
                        .uri(authServiceUrl))

                // Customer Support - PUBLIC (oricine poate trimite mesaj)
                .route("support", r -> r.path("/api/support/**")
                        .uri(supportServiceUrl))

                // WebSocket endpoint - PUBLIC (pentru conexiuni WS)
                .route("websocket-connection", r -> r.path("/ws/**")
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri(websocketServiceUrl))




                // Rute protejate (autentificare + autorizare)

                .route("websocket-api", r -> r.path("/api/websocket/**")
                        .filters(f -> f
                                .filter(authFilter)
                                .filter(authorizationFilter))
                        .uri(websocketServiceUrl))

                .route("users", r -> r.path("/api/users/**")
                        .filters(f -> f
                                .filter(authFilter)
                                .filter(authorizationFilter))
                        .uri(userServiceUrl))

                .route("devices", r -> r.path("/api/devices/**")
                        .filters(f -> f
                                .filter(authFilter)
                                .filter(authorizationFilter))
                        .uri(deviceServiceUrl))

                .build();
    }
}