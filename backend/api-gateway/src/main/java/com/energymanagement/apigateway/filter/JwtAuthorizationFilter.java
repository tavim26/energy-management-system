package com.energymanagement.apigateway.filter;

import com.energymanagement.apigateway.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Applied to protected routes: the token must be valid (401 otherwise)
// and its role must allow the request (403 otherwise)
@Component
public class JwtAuthorizationFilter implements GatewayFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Pattern CLIENT_DEVICES_PATH = Pattern.compile("^/api/devices/user/(\\d+)$");

    private final JwtService jwtService;

    public JwtAuthorizationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        Optional<Claims> claims = extractToken(request).flatMap(jwtService::parse);

        if (claims.isEmpty()) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Missing, invalid or expired token");
        }

        if (!isAllowed(request, claims.get())) {
            return reject(exchange, HttpStatus.FORBIDDEN, "You do not have access to this resource");
        }

        return chain.filter(exchange);
    }

    // ADMIN can access everything; CLIENT can only read the list of its own devices
    private boolean isAllowed(ServerHttpRequest request, Claims claims) {
        String role = claims.get("role", String.class);

        if ("ADMIN".equals(role)) {
            return true;
        }

        if (!"CLIENT".equals(role) || request.getMethod() != HttpMethod.GET) {
            return false;
        }

        Matcher matcher = CLIENT_DEVICES_PATH.matcher(request.getPath().value());
        Number userId = claims.get("userId", Number.class);

        return matcher.matches()
                && userId != null
                && matcher.group(1).equals(String.valueOf(userId.longValue()));
    }

    private Optional<String> extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        return Optional.of(header.substring(BEARER_PREFIX.length()));
    }

    // Same JSON format as the errors returned by the microservices
    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"status\":%d,\"message\":\"%s\",\"timestamp\":\"%s\"}"
                .formatted(status.value(), message, Instant.now());

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}