package com.jakeer.user_service.service;

import com.jakeer.user_service.dto.UserRequestDto;
import com.jakeer.user_service.dto.UserResponseDto;
import com.jakeer.user_service.entity.User;
import com.jakeer.user_service.exception.UserNotFoundException;
import com.jakeer.user_service.mapper.UserMapper;
import com.jakeer.user_service.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final UserMapper userMapper;

    public UserService(
            UserRepository userRepo,
            UserMapper userMapper) {

        this.userRepo = userRepo;
        this.userMapper = userMapper;
    }

    // =========================================================
    // GET ALL ACTIVE USERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {

        return userRepo.findByDeletedFalse()
                .stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    // =========================================================
    // GET USERS WITH PAGINATION AND SORTING
    // =========================================================

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getUsersWithPaginationAndSorting(
            int page,
            int size,
            String sortBy,
            String direction) {

        validatePageAndSize(page, size);
        validateSort(sortBy, direction);

        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage =
                userRepo.findByDeletedFalse(pageable);

        return userPage.map(userMapper::toResponseDto);
    }

    // =========================================================
    // SEARCH ACTIVE USERS WITH PAGINATION AND SORTING
    // =========================================================

    @Transactional(readOnly = true)
    public Page<UserResponseDto> searchUsers(
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction) {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Search keyword must not be empty");
        }

        validatePageAndSize(page, size);
        validateSort(sortBy, direction);

        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage =
                userRepo.searchUsers(keyword.trim(), pageable);

        return userPage.map(userMapper::toResponseDto);
    }

    // =========================================================
    // GET USER BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Integer userId) {

        User user = userRepo
                .findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + userId
                        ));

        return userMapper.toResponseDto(user);
    }

    // =========================================================
    // CREATE USER
    // =========================================================

    @Transactional
    public UserResponseDto createUser(
            UserRequestDto userRequestDto) {

        User user = userMapper.toEntity(userRequestDto);

        // New users should always be active
        user.setDeleted(false);

        if (user.getUserAccStatus() == null ||
                user.getUserAccStatus().trim().isEmpty()) {

            user.setUserAccStatus("ACTIVE");
        }

        User savedUser = userRepo.save(user);

        return userMapper.toResponseDto(savedUser);
    }

    // =========================================================
    // UPDATE USER
    // =========================================================

    @Transactional
    public UserResponseDto updateUser(
            Integer userId,
            UserRequestDto userRequestDto) {

        User existingUser = userRepo
                .findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + userId
                        ));

        existingUser.setUserFirstName(
                userRequestDto.getUserFirstName());

        existingUser.setUserLastName(
                userRequestDto.getUserLastName());

        existingUser.setUserEmail(
                userRequestDto.getUserEmail());

        existingUser.setUserPhno(
                userRequestDto.getUserPhno());

        existingUser.setUserDOB(
                userRequestDto.getUserDOB());

        existingUser.setUserGender(
                userRequestDto.getUserGender());

        existingUser.setUserCountry(
                userRequestDto.getUserCountry());

        existingUser.setUserState(
                userRequestDto.getUserState());

        existingUser.setUserCity(
                userRequestDto.getUserCity());

        existingUser.setUserAccStatus(
                userRequestDto.getUserAccStatus());

        /*
         * Password is intentionally not updated here.
         * Password update should be handled by Auth Service.
         */

        User updatedUser = userRepo.save(existingUser);

        return userMapper.toResponseDto(updatedUser);
    }

    // =========================================================
    // SOFT DELETE USER
    // =========================================================

    @Transactional
    public void deleteUser(Integer userId) {

        User existingUser = userRepo
                .findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + userId
                        ));

        existingUser.setDeleted(true);
        existingUser.setUserAccStatus("DELETED");

        userRepo.save(existingUser);
    }

    // =========================================================
    // RESTORE SOFT-DELETED USER
    // =========================================================

    @Transactional
    public UserResponseDto restoreUser(Integer userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + userId
                        ));

        user.setDeleted(false);
        user.setUserAccStatus("ACTIVE");

        User restoredUser = userRepo.save(user);

        return userMapper.toResponseDto(restoredUser);
    }

    // =========================================================
    // PAGE AND SIZE VALIDATION
    // =========================================================

    private void validatePageAndSize(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }
    }

    // =========================================================
    // SORT VALIDATION
    // =========================================================

    private void validateSort(
            String sortBy,
            String direction) {

        if (!isAllowedSortField(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + sortBy);
        }

        if (!"asc".equalsIgnoreCase(direction)
                && !"desc".equalsIgnoreCase(direction)) {

            throw new IllegalArgumentException(
                    "Sort direction must be asc or desc");
        }
    }

    // =========================================================
    // ALLOWED SORT FIELDS
    // =========================================================

    private boolean isAllowedSortField(String sortBy) {

        return "userId".equals(sortBy)
                || "userFirstName".equals(sortBy)
                || "userLastName".equals(sortBy)
                || "userEmail".equals(sortBy);
    }
}