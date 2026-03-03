package com.energymanagement.usermanagement.service;

import com.energymanagement.usermanagement.client.AuthServiceClient;
import com.energymanagement.usermanagement.dto.CreateUserDTO;
import com.energymanagement.usermanagement.dto.UpdateUserDTO;
import com.energymanagement.usermanagement.dto.UserDTO;
import com.energymanagement.usermanagement.model.User;
import com.energymanagement.usermanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService
{

    private final UserRepository userRepository;
    private final AuthServiceClient authServiceClient;


    // RabbitTemplate pentru publicare evenimente de sincronizare
    private final RabbitTemplate syncRabbitTemplate;

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchangeName;

    public UserService(
            UserRepository userRepository,
            AuthServiceClient authServiceClient,
            @Qualifier("syncRabbitTemplate") RabbitTemplate syncRabbitTemplate
    ) {
        this.userRepository = userRepository;
        this.authServiceClient = authServiceClient;
        this.syncRabbitTemplate = syncRabbitTemplate;
    }

    // Creare user nou
    @Transactional
    public UserDTO createUser(CreateUserDTO createUserDTO)
    {
        // Validari
        if (createUserDTO.getUsername() == null || createUserDTO.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username cannot be empty");
        }

        if (createUserDTO.getPassword() == null || createUserDTO.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }

        // Creare user in user_db
        User user = new User();
        user.setFullName(createUserDTO.getFullName());
        user.setAddress(createUserDTO.getAddress());

        User savedUser = userRepository.save(user);

        String role = (createUserDTO.getRole() != null && !createUserDTO.getRole().trim().isEmpty())
                ? createUserDTO.getRole()
                : "CLIENT";

        // Publicare Sync Event USER_CREATED
        publishUserCreatedEvent(savedUser, createUserDTO.getUsername(), createUserDTO.getPassword(), role);

        // Return UserDTO complet
        return convertToDTO(savedUser, createUserDTO.getUsername(), role);
    }




    // Returneaza toti userii cu tot cu credentials
    public List<UserDTO> getAllUsers()
    {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> {
                    // Apeleaza Auth Service pentru username si role
                    Map<String, String> credentials = authServiceClient.getUserCredentials(user.getId());

                    if (credentials != null)
                    {
                        return convertToDTO(user, credentials.get("username"), credentials.get("role"));
                    }
                    else
                    {
                        return convertToDTO(user, null, null);
                    }
                })
                .collect(Collectors.toList());
    }

    public Optional<UserDTO> getUserById(Long id)
    {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Apeleaza Auth Service pentru username si role
            Map<String, String> credentials = authServiceClient.getUserCredentials(user.getId());

            if (credentials != null)
            {
                UserDTO userDTO = convertToDTO(user, credentials.get("username"), credentials.get("role"));
                return Optional.of(userDTO);

            }
            else
            {
                UserDTO userDTO = convertToDTO(user, null, null);
                return Optional.of(userDTO);
            }
        }

        return Optional.empty();
    }



    public UserDTO updateUser(Long id, UpdateUserDTO updateUserDTO)
    {
        Optional<User> userOptional = userRepository.findById(id);

        if (!userOptional.isPresent())
        {
            throw new RuntimeException("User not found with id: " + id);
        }

        User user = userOptional.get();

        if (updateUserDTO.getFullName() != null)
        {
            user.setFullName(updateUserDTO.getFullName());
        }

        if (updateUserDTO.getAddress() != null)
        {
            user.setAddress(updateUserDTO.getAddress());
        }

        User updatedUser = userRepository.save(user);

        return convertToDTO(updatedUser, null, null);
    }

    // Delete user
    @Transactional
    public void deleteUser(Long id)
    {
        if (!userRepository.existsById(id))
        {
            throw new RuntimeException("User not found with id: " + id);
        }

        // Sterge user din user_db
        userRepository.deleteById(id);

        // Publicare eveniment USER_DELETED
        publishUserDeletedEvent(id);

        System.out.println("User deleted successfully with id: " + id);
    }





    // Creare user fara credentials (apelat de Auth Service la register)
    // Returneaza doar userId pentru ca Auth Service sa creeze credentials
    @Transactional
    public Long createUserAndGetId(String fullName, String address)
    {
        User user = new User();
        user.setFullName(fullName);
        user.setAddress(address);

        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }


    // Publicare eveniment USER_CREATED pentru sincronizare
    private void publishUserCreatedEvent(User user, String username, String password, String role)
    {
        try {
            Map<String, Object> syncMessage = new HashMap<>();
            syncMessage.put("eventType", "USER_CREATED");
            syncMessage.put("userId", user.getId());
            syncMessage.put("fullName", user.getFullName());
            syncMessage.put("address", user.getAddress());
            syncMessage.put("username", username);
            syncMessage.put("password", password);
            syncMessage.put("role", role);

            // Publica pe Sync Exchange cu routing key gol (fanout broadcast)
            syncRabbitTemplate.convertAndSend(syncExchangeName, "", syncMessage);

            System.out.println("Published USER_CREATED event for user ID: " + user.getId());

        } catch (Exception e) {

            System.err.println("Failed to publish USER_CREATED event: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Publicare eveniment USER_DELETED pentru sincronizare
    private void publishUserDeletedEvent(Long userId)
    {
        try {
            Map<String, Object> syncMessage = new HashMap<>();
            syncMessage.put("eventType", "USER_DELETED");
            syncMessage.put("userId", userId);

            // Publica pe Sync Exchange (fanout broadcast)
            syncRabbitTemplate.convertAndSend(syncExchangeName, "", syncMessage);

            System.out.println("Published USER_DELETED event for user ID: " + userId);

        } catch (Exception e) {

            System.err.println("Failed to publish USER_DELETED event: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private UserDTO convertToDTO(User user, String username, String role) {
        return new UserDTO(
                user.getId(),
                username,
                role,
                user.getFullName(),
                user.getAddress()
        );
    }
}