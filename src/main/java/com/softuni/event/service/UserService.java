package com.softuni.event.service;

import com.softuni.event.model.dto.UserProfileDTO;
import com.softuni.event.model.dto.UserRegisterDTO;

import java.util.List;

public interface UserService {
    void registerUser(UserRegisterDTO userRegisterDTO);
    UserProfileDTO getUserProfile(String username);
    UserProfileDTO getUserById(Long id);
    void updateUserProfile(UserProfileDTO userProfileDTO);
    boolean isUsernameExists(String username);
    boolean isEmailExists(String email);
    List<UserProfileDTO> getAllUsers();
    void changeUserRole(Long userId, String role);
} 