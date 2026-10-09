package com.energymanagement.usermanagement.controller;

import com.energymanagement.usermanagement.dto.CreateProfileDTO;
import com.energymanagement.usermanagement.dto.CreatedProfileDTO;
import com.energymanagement.usermanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Called only by the Authorization Service. The gateway has no route for /internal/**,
// so these endpoints cannot be reached through it.
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<CreatedProfileDTO> createProfile(@Valid @RequestBody CreateProfileDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreatedProfileDTO(userService.createProfile(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        userService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}