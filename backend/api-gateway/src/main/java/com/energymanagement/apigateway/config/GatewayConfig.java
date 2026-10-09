package com.energymanagement.apigateway.config;

import com.energymanagement.apigateway.filter.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Single entry point for the frontend: every request is forwarded to the matching microservice.
// Routes without the JWT filter are public. /internal/** is intentionally not routed.
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(
            RouteLocatorBuilder builder,
            JwtAuthorizationFilter jwtFilter,
            @Value("${service.auth.url}") String authServiceUrl,
            @Value("${service.user.url}") String userServiceUrl,
            @Value("${service.device.url}") String deviceServiceUrl,
            @Value("${service.support.url}") String supportServiceUrl,
            @Value("${service.websocket.url}") String websocketServiceUrl
    ) {
        return builder.routes()
                .route("auth-public", r -> r.path("/api/auth/login", "/api/auth/register")
                        .uri(authServiceUrl))

                .route("auth-admin", r -> r.path("/api/auth/users")
                        .filters(f -> f.filter(jwtFilter))
                        .uri(authServiceUrl))

                .route("users", r -> r.path("/api/users/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri(userServiceUrl))

                .route("devices", r -> r.path("/api/devices/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri(deviceServiceUrl))

                .route("support", r -> r.path("/api/support/**")
                        .uri(supportServiceUrl))

                // SockJS handshake and WebSocket connection. The WebSocket Service adds its own
                // CORS headers, so the duplicates added by the gateway are removed.
                .route("websocket", r -> r.path("/ws/**")
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri(websocketServiceUrl))

                .route("websocket-api", r -> r.path("/api/websocket/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri(websocketServiceUrl))

                .build();
    }
}