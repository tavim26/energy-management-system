package com.energymanagement.apigateway.filter;

import com.energymanagement.apigateway.service.JwtValidationService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


  // AuthorizationFilter - al doilea filtru; verifica permisiunile

  // ADMIN: are acces la toate resursele
  // CLIENT: are acces doar la propriile device-uri

@Component
public class AuthorizationFilter implements GatewayFilter
{

    private final JwtValidationService jwtValidationService;

    public AuthorizationFilter(JwtValidationService jwtValidationService)
    {
        this.jwtValidationService = jwtValidationService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)
    {
        ServerHttpRequest request = exchange.getRequest();

        // Extrage token-ul din header
        String token = extractToken(request);

        if (token == null) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // Extrage rolul din token
        String role = jwtValidationService.getRoleFromToken(token);

        if (role == null)
        {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // Verifica daca user-ul are permisiuni pentru cerere
        if (!hasPermission(request, role, token))
        {
            return onError(exchange, HttpStatus.FORBIDDEN);
        }

        // User-ul are permisiuni -> continua cu microserviciul corespunzator
        return chain.filter(exchange);
    }

     // Verifica daca user-ul are permisiuni pentru cererea curenta
    private boolean hasPermission(ServerHttpRequest request, String role, String token)
    {
        String path = request.getPath().value();
        HttpMethod method = request.getMethod();

        // ADMIN are acces la toate resursele
        if ("ADMIN".equals(role))
        {
            return true;
        }

        if ("CLIENT".equals(role))
        {
            return checkClientPermissions(path, method, token);
        }

        return false;
    }

     //Verifica permisiunile pentru CLIENT (poate doar sa vada propriile device-uri)
    private boolean checkClientPermissions(String path, HttpMethod method, String token)
    {
        // extrage userId din token
        Long userId = jwtValidationService.getUserIdFromToken(token);

        // Verifica daca CLIENT incearca sa vada device-urile sale
        if (path.startsWith("/api/devices/user/") && method == HttpMethod.GET)
        {

            String userIdFromPath = extractUserIdFromPath(path);

            // Verifica user id din token e identic cu cel din path
            return userIdFromPath != null && userIdFromPath.equals(String.valueOf(userId));
        }

        return false;
    }


    private String extractUserIdFromPath(String path)
    {
        // Path: /api/devices/user/5 → parts: ["", "api", "devices", "user", "5"]
        String[] parts = path.split("/");

        if (parts.length >= 5 && "user".equals(parts[3]))
        {
            return parts[4];
        }

        return null;
    }

     // Extrage token-ul JWT din header Authorization
    private String extractToken(ServerHttpRequest request)
    {
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION))
        {
            return null;
        }

        String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            return null;
        }

        // Returneaza doar token-ul (fara "Bearer ")
        return authHeader.substring(7);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status)
    {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }
}