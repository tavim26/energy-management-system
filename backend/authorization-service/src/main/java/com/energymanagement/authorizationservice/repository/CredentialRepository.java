package com.energymanagement.authorizationservice.repository;

import com.energymanagement.authorizationservice.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialRepository extends JpaRepository<Credential, Long> {

    Optional<Credential> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByRole(String role);
}