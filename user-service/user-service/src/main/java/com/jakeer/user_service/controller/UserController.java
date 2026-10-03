package com.jakeer.user_service.controller;

import com.jakeer.user_service.dto.UserRequestDto;
import com.jakeer.user_service.dto.UserResponseDto;
import com.jakeer.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {

        List<UserResponseDto> users = userService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    // Pagination + Sorting
    @GetMapping("/pagination-sort")
    public ResponseEntity<Page<UserResponseDto>> getUsersWithPaginationAndSorting(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Page<UserResponseDto> response =
                userService.getUsersWithPaginationAndSorting(
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(response);
    }

    // Search + Pagination + Sorting
    @GetMapping("/search")
    public ResponseEntity<Page<UserResponseDto>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Page<UserResponseDto> response =
                userService.searchUsers(
                        keyword,
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(response);
    }

    // Get user by ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable Integer userId) {

        UserResponseDto response =
                userService.getUserById(userId);

        return ResponseEntity.ok(response);
    }

    // Create user
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserRequestDto userRequestDto) {

        UserResponseDto response =
                userService.createUser(userRequestDto);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

    // Update user
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Integer userId,
            @Valid @RequestBody UserRequestDto userRequestDto) {

        UserResponseDto response =
                userService.updateUser(
                        userId,
                        userRequestDto
                );

        return ResponseEntity.ok(response);
    }

    // Delete user
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Integer userId) {

        userService.deleteUser(userId);

        return ResponseEntity.ok(
                "User deleted successfully with id: " + userId
        );
    }
}