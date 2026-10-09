package com.energymanagement.apigateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Optional;

// Validates the tokens issued by the Authorization Service (same secret on both sides)
@Service
public class JwtService {

    private final Key signingKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Checks the signature and the expiration date; empty if the token is not valid
    public Optional<Claims> parse(String token) {
        try {
            return Optional.of(Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody());

        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}