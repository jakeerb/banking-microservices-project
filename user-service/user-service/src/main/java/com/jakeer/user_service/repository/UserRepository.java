package com.jakeer.user_service.repository;

import com.jakeer.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Integer> {

    // Check duplicate email while creating user
    boolean existsByUserEmail(String userEmail);

    // Check duplicate email while updating user
    boolean existsByUserEmailAndUserIdNot(
            String userEmail,
            Integer userId);

    // Get only active/non-deleted users
    List<User> findByDeletedFalse();

    // Get active user by ID
    Optional<User> findByUserIdAndDeletedFalse(Integer userId);

    // Pagination with only active users
    Page<User> findByDeletedFalse(Pageable pageable);

    // Search only active users
    @Query("""
        SELECT u FROM User u
        WHERE u.deleted = false
        AND (
            LOWER(u.userFirstName) LIKE
                LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.userLastName) LIKE
                LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.userEmail) LIKE
                LOWER(CONCAT('%', :keyword, '%'))
        )
        """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            Pageable pageable);
}