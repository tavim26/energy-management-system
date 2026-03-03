package com.energymanagement.usermanagement.repository;

import com.energymanagement.usermanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // metodele de baza JPA: findById, findAll, save, delete
}