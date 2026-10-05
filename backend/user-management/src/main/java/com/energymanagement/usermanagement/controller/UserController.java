package com.energymanagement.usermanagement.controller;

import com.energymanagement.usermanagement.dto.UpdateUserDTO;
import com.energymanagement.usermanagement.dto.UserDTO;
import com.energymanagement.usermanagement.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController
{

    private final UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }



    //retureaza toti userii
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers()
    {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    //cauta user dupa ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id)
    {
        Optional<UserDTO> userOptional = userService.getUserById(id);

        if (userOptional.isPresent())
        {
            return ResponseEntity.ok(userOptional.get());
        }
        else
        {
            return ResponseEntity.notFound().build();
        }
    }


    //update user
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UpdateUserDTO updateUserDTO)
    {
        try {
            UserDTO updatedUser = userService.updateUser(id, updateUserDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    //delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id)
    {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



    // Endpoint special pentru Auth Service (la register)
    // Creeaza user fara credentials, returneaza doar userId
    @PostMapping("/create-basic")
    public ResponseEntity<Map<String, Long>> createBasicUser(@RequestBody Map<String, String> request)
    {
        try {
            String fullName = request.get("fullName");
            String address = request.get("address");

            Long userId = userService.createUserAndGetId(fullName, address);

            Map<String, Long> response = new HashMap<>();
            response.put("userId", userId);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}