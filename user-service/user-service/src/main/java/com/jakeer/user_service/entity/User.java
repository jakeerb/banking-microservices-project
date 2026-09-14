package com.jakeer.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;
@Entity
@Table(name="user_dtls")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Integer userId;

    @NotBlank(message = "First name is required")
    @Column(name = "USER_FIRST_NAME")
    private String userFirstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "USER_LAST_NAME")
    private String userLastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid Email")
    @Column(name = "USER_EMAIL", unique = true)
    private String userEmail;

    @NotNull(message = "Phone number is required")
    @Column(name = "USER_PHNO")
    private Long userPhno;

    @NotNull(message = "Date of birth is required")
    @Column(name = "USER_DOB")
    private Date userDOB;

    @NotBlank(message = "Gender is required")
    @Column(name = "USER_GENDER")
    private String userGender;

    @NotNull(message = "Country is required")
    @Column(name = "USER_COUNTRY")
    private Integer userCountry;

    @NotNull(message = "State is required")
    @Column(name = "USER_STATE")
    private Integer userState;

    @Column(name = "USER_CITY")
    private Integer userCity;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(name = "USERPASSWORD")
    private String userPassword;

    @Column(name = "USER_ACC_STATUS")
    private String userAccStatus;

    @Column(name = "CREATE_DATE")
    private Date createdDate;

    @Column(name = "UPDATE_DATE")
    private Date updatedDate;


    @Column(name="LOGIN_ATTEMPTS")
    private Integer loginAttempts = 0;
}
