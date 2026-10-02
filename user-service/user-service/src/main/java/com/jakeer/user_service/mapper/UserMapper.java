package com.jakeer.user_service.mapper;

import com.jakeer.user_service.dto.UserRequestDto;
import com.jakeer.user_service.dto.UserResponseDto;
import com.jakeer.user_service.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequestDto request) {

        User user = new User();

        user.setUserFirstName(request.getUserFirstName());
        user.setUserLastName(request.getUserLastName());
        user.setUserEmail(request.getUserEmail());
        user.setUserPhno(request.getUserPhno());
        user.setUserDOB(request.getUserDOB());
        user.setUserGender(request.getUserGender());
        user.setUserCountry(request.getUserCountry());
        user.setUserState(request.getUserState());
        user.setUserCity(request.getUserCity());
        user.setUserAccStatus(request.getUserAccStatus());

        return user;
    }

    public UserResponseDto toResponseDto(User user) {

        UserResponseDto response = new UserResponseDto();

        response.setUserId(user.getUserId());
        response.setUserFirstName(user.getUserFirstName());
        response.setUserLastName(user.getUserLastName());
        response.setUserEmail(user.getUserEmail());
        response.setUserPhno(user.getUserPhno());
        response.setUserDOB(user.getUserDOB());
        response.setUserGender(user.getUserGender());
        response.setUserCountry(user.getUserCountry());
        response.setUserState(user.getUserState());
        response.setUserCity(user.getUserCity());
        response.setUserAccStatus(user.getUserAccStatus());

        return response;
    }
}