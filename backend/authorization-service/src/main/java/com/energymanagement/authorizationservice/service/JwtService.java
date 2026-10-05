package com.energymanagement.authorizationservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.JwtException;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService
{

    // secret key pentru token signature
    @Value("${jwt.secret}")
    private String secret;

    // timp de expirare ptr token
    @Value("${jwt.expiration}")
    private Long expiration;

    // genereaza un jwt pentru un utilizator
    public String generateToken(Long userId, String username, String role)
    {
        // claims = informatiile care sunt in token
        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        Date now = new Date();

        // data expirarii
        Date expiryDate = new Date(now.getTime() + expiration);

        // construire token si return
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)    //semneaza token-ul
                .compact();
    }


    // Returns null if the token is invalid or expired
    public String extractRole(String token)
    {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("role", String.class);

        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }




    private Key getSigningKey()
    {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}