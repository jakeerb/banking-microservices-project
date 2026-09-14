package com.jakeer.authservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "AUTH_USERS")
@Data
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean enabled = true;
}