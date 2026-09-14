package com.jakeer.authservice.repository;

import com.jakeer.authservice.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<AuthUser,Long> {

    Optional<AuthUser> findByEmail(String email);

    boolean existsByEmail(String email);

}
