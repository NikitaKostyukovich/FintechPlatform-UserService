package com.innowise.userservice.controller;

import com.innowise.userservice.dto.user.UserRequestDTO;
import com.innowise.userservice.dto.user.UserResponseDTO;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getFilteredUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable) {
        Page<UserResponseDTO> users = userService.getFilteredUsers(name, surname, pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO dto){
        UserResponseDTO createdUser = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserRequestDTO dto){
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam boolean status){
        userService.updateUserStatus(id, status);
        return ResponseEntity.ok().build();
    }

}
