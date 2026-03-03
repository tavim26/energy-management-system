package com.energymanagement.apigateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;

//Valideaza token-ul (verifica signature si expirare)
//Extrage informatii din token (userId, username, role)
//Format: header.payload.signature
//Payload contine: userId, username, role, expirare, etc.
@Service
public class JwtValidationService
{

    // Secret key pentru verificarea signature
    @Value("${jwt.secret}")
    private String secret;


     //Valideaza token-ul JWT
     // Verifica:
     // 1. Signature
     // 2. Expirare
     // 3. Format
    public boolean validateToken(String token)
    {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) //foloseste jwt.secret
                    .build()
                    .parseClaimsJws(token); //verifica signature si expirare

            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public String getRoleFromToken(String token)
    {
        try {
            Claims claims = extractClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }


    public Long getUserIdFromToken(String token)
    {
        try {
            Claims claims = extractClaims(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }




    //Extrage toate informatiile (claims) din token

    private Claims extractClaims(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    private Key getSigningKey()
    {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}