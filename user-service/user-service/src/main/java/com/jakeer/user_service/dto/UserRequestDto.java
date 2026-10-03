package com.jakeer.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;


@Data
public class UserRequestDto {

    @NotBlank(message = "First name is required")
    private String userFirstName;

    @NotBlank(message = "Last name is required")
    private String userLastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String userEmail;

    @NotNull(message = "Phone number is required")
    private Long userPhno;

    @NotNull(message = "Date of birth is required")
    private Date userDOB;

    @NotBlank(message = "Gender is required")
    private String userGender;

    @NotNull(message = "Country is required")
    private Integer userCountry;

    @NotNull(message = "State is required")
    private Integer userState;

    private Integer userCity;

    private String userAccStatus;

    // Getters and Setters
}