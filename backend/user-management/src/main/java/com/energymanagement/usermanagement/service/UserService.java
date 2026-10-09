package com.energymanagement.usermanagement.service;

import com.energymanagement.usermanagement.client.AuthServiceClient;
import com.energymanagement.usermanagement.dto.CreateProfileDTO;
import com.energymanagement.usermanagement.dto.CredentialDTO;
import com.energymanagement.usermanagement.dto.UpdateUserDTO;
import com.energymanagement.usermanagement.dto.UserDTO;
import com.energymanagement.usermanagement.event.UserEventPublisher;
import com.energymanagement.usermanagement.exception.ConflictException;
import com.energymanagement.usermanagement.exception.ResourceNotFoundException;
import com.energymanagement.usermanagement.model.User;
import com.energymanagement.usermanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final AuthServiceClient authServiceClient;
    private final UserEventPublisher eventPublisher;

    public UserService(
            UserRepository userRepository,
            AuthServiceClient authServiceClient,
            UserEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.authServiceClient = authServiceClient;
        this.eventPublisher = eventPublisher;
    }

    // A single call to the Authorization Service, whatever the number of users
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        Map<Long, CredentialDTO> credentialsById = authServiceClient.getAllCredentials().stream()
                .collect(Collectors.toMap(CredentialDTO::userId, Function.identity()));

        return userRepository.findAll().stream()
                .map(user -> UserDTO.from(user, credentialsById.get(user.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = findUser(id);
        return UserDTO.from(user, authServiceClient.getCredentials(id).orElse(null));
    }

    @Transactional
    public UserDTO updateUser(Long id, UpdateUserDTO request) {
        User user = findUser(id);

        if (request.fullName() != null) {
            user.setFullName(request.fullName().trim());
        }

        if (request.address() != null) {
            user.setAddress(request.address().trim());
        }

        User saved = userRepository.save(user);

        return UserDTO.from(saved, authServiceClient.getCredentials(id).orElse(null));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findUser(id);
        ensureNotLastAdmin(id);

        userRepository.delete(user);
        eventPublisher.userDeleted(id);
    }

    // Called by the Authorization Service during registration
    @Transactional
    public Long createProfile(CreateProfileDTO request) {
        User user = userRepository.save(new User(trimToNull(request.fullName()), trimToNull(request.address())));
        return user.getId();
    }

    // Called by the Authorization Service to undo createProfile when a registration fails.
    // No event is published: the other services never heard about this user.
    @Transactional
    public void deleteProfile(Long id) {
        userRepository.deleteById(id);
    }

    // Deleting the only admin would leave nobody able to manage the application
    private void ensureNotLastAdmin(Long id) {
        List<CredentialDTO> credentials = authServiceClient.getAllCredentials();

        boolean isAdmin = credentials.stream()
                .anyMatch(credential -> credential.userId().equals(id) && ROLE_ADMIN.equals(credential.role()));

        long adminCount = credentials.stream()
                .filter(credential -> ROLE_ADMIN.equals(credential.role()))
                .count();

        if (isAdmin && adminCount <= 1) {
            throw new ConflictException("The last admin account cannot be deleted");
        }
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " was not found"));
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}