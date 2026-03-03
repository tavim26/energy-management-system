package com.energymanagement.authorizationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserServiceClient
{

    @Value("${user.service.url}")
    private String userServiceUrl;

    private final RestTemplate restTemplate;

    public UserServiceClient()
    {
        this.restTemplate = new RestTemplate();
    }

    // Apeleaza User Service pentru a crea user
    public Long createUserAndGetId(String fullName, String address)
    {
        String url = userServiceUrl + "/api/users/create-basic";

        Map<String, String> request = new HashMap<>();
        request.put("fullName", fullName);
        request.put("address", address);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null)
            {
                Number userId = (Number) response.getBody().get("userId");
                return userId.longValue();
            }

            throw new RuntimeException("Failed to create user in User Service");

        } catch (Exception e) {
            System.err.println("Error calling User Service: " + e.getMessage());
            throw new RuntimeException("Failed to create user in User Service: " + e.getMessage());
        }
    }
}