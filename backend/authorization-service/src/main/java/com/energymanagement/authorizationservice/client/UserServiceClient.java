package com.energymanagement.authorizationservice.client;

import com.energymanagement.authorizationservice.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

// Calls the internal endpoints of the User Service, which stores the profile data (full name, address)
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestClient restClient;

    public UserServiceClient(@Value("${user.service.url}") String userServiceUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .baseUrl(userServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    // Creates the profile and returns the id generated for the new user
    public Long createProfile(String fullName, String address) {
        try {
            CreatedProfile response = restClient.post()
                    .uri("/internal/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ProfileRequest(fullName, address))
                    .retrieve()
                    .body(CreatedProfile.class);

            if (response == null || response.userId() == null) {
                throw new ExternalServiceException("User service returned an empty response", null);
            }

            return response.userId();

        } catch (RestClientException e) {
            throw new ExternalServiceException("User service is unavailable", e);
        }
    }

    // Used to undo createProfile when the credentials cannot be saved
    public void deleteProfile(Long userId) {
        try {
            restClient.delete()
                    .uri("/internal/users/{id}", userId)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException e) {
            log.error("Could not delete the profile of user {} after a failed registration", userId, e);
        }
    }

    public record ProfileRequest(String fullName, String address) {
    }

    public record CreatedProfile(Long userId) {
    }
}