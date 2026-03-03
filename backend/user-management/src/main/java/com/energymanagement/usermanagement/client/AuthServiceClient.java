package com.energymanagement.usermanagement.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;

@Component
public class AuthServiceClient
{

    @Value("${auth.service.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate;

    public AuthServiceClient()
    {
        this.restTemplate = new RestTemplate();
    }

    // Apeleaza Auth Service pentru a obtine username si role pentru un user
    public Map<String, String> getUserCredentials(Long userId)
    {
        String url = authServiceUrl + "/api/auth/credentials/" + userId;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null)
            {
                return (Map<String, String>) response.getBody();
            }

            return null;

        } catch (Exception e) {

            System.err.println("Error fetching credentials for user " + userId + ": " + e.getMessage());
            return null;
        }
    }
}