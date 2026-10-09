package com.energymanagement.usermanagement.client;

import com.energymanagement.usermanagement.dto.CredentialDTO;
import com.energymanagement.usermanagement.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

// Reads usernames and roles from the internal endpoints of the Authorization Service
@Component
public class AuthServiceClient {

    private final RestClient restClient;

    public AuthServiceClient(@Value("${auth.service.url}") String authServiceUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .baseUrl(authServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public List<CredentialDTO> getAllCredentials() {
        try {
            List<CredentialDTO> credentials = restClient.get()
                    .uri("/internal/credentials")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CredentialDTO>>() {
                    });

            return credentials == null ? List.of() : credentials;

        } catch (RestClientException e) {
            throw new ExternalServiceException("Authorization service is unavailable", e);
        }
    }

    public Optional<CredentialDTO> getCredentials(Long userId) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/internal/credentials/{id}", userId)
                    .retrieve()
                    .body(CredentialDTO.class));

        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();

        } catch (RestClientException e) {
            throw new ExternalServiceException("Authorization service is unavailable", e);
        }
    }
}