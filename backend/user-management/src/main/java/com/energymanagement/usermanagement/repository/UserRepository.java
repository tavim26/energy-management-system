package com.energymanagement.usermanagement.repository;

import com.energymanagement.usermanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}