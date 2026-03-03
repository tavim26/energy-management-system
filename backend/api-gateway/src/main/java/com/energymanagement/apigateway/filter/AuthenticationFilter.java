package com.energymanagement.apigateway.filter;

import com.energymanagement.apigateway.service.JwtValidationService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

 // AuthenticationFilter - primul filtru care verifica token-ul JWT
 // Verifica daca user-ul este autentificat (token JWT valid)
@Component
public class AuthenticationFilter implements GatewayFilter
 {

    private final JwtValidationService jwtValidationService;

    public AuthenticationFilter(JwtValidationService jwtValidationService)
    {
        this.jwtValidationService = jwtValidationService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Verifica daca exista header Authorization
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION))
        {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // Extrage header-ul
        String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

        //verifica format
        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // ia token-ul
        String token = authHeader.substring(7);

        // Valideaza token-ul (signature si expirare)
        if (!jwtValidationService.validateToken(token))
        {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // daca e token valid, continua cu urmatorul filtru (AuthorizationFilter)
        return chain.filter(exchange);
    }


    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status)
    {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }
}