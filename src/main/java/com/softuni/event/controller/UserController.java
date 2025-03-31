package com.softuni.event.controller;

import com.softuni.event.model.dto.UserProfileDTO;
import com.softuni.event.model.dto.UserRegisterDTO;
import com.softuni.event.service.NotificationService;
import com.softuni.event.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final NotificationService notificationService;

    public UserController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("userRegisterDTO", new UserRegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userRegisterDTO") UserRegisterDTO userRegisterDTO,
                          BindingResult bindingResult) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // Check if username already exists
        if (userService.isUsernameExists(userRegisterDTO.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Username already exists");
            return "register";
        }

        // Check if email already exists
        if (userService.isEmailExists(userRegisterDTO.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email already exists");
            return "register";
        }

        // Register user
        userService.registerUser(userRegisterDTO);
        return "redirect:/users/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserProfileDTO userProfile = userService.getUserProfile(userDetails.getUsername());
        model.addAttribute("user", userProfile);
        model.addAttribute("notifications", 
                notificationService.getRecentNotifications(userDetails.getUsername()));
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserProfileDTO userProfile = userService.getUserProfile(userDetails.getUsername());
        model.addAttribute("userProfileDTO", userProfile);
        return "edit-profile";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("userProfileDTO") UserProfileDTO userProfileDTO,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "edit-profile";
        }

        userService.updateUserProfile(userProfileDTO);
        return "redirect:/users/profile";
    }

    @GetMapping("/admin/users")
    public String adminUsersList(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-users";
    }

    @PostMapping("/admin/users/{id}/change-role")
    public String changeUserRole(@PathVariable Long id, @RequestParam String role) {
        userService.changeUserRole(id, role);
        return "redirect:/users/admin/users";
    }
}