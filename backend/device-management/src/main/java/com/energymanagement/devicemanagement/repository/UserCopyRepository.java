package com.energymanagement.devicemanagement.repository;

import com.energymanagement.devicemanagement.model.UserCopy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCopyRepository extends JpaRepository<UserCopy, Long> {
}