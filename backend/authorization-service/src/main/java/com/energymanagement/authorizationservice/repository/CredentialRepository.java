package com.energymanagement.authorizationservice.repository;

import com.energymanagement.authorizationservice.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long>
{

    // Cauta credential dupa username (pentru login)
    Optional<Credential> findByUsername(String username);

    // Verifica daca username-ul exista deja
    boolean existsByUsername(String username);

    boolean existsByRole(String role);


}