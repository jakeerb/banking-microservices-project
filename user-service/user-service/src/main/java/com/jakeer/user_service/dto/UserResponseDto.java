package com.jakeer.user_service.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserResponseDto {

    private Integer userId;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private Long userPhno;
    private Date userDOB;
    private String userGender;
    private Integer userCountry;
    private Integer userState;
    private Integer userCity;
    private String userAccStatus;

    // Getters and Setters
}