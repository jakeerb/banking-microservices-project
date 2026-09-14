package com.jakeer.user_service.service;


import com.jakeer.user_service.entity.User;
import com.jakeer.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private UserRepository userRepo;

    public UserService(UserRepository userRepo){
        this.userRepo=userRepo;
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}
