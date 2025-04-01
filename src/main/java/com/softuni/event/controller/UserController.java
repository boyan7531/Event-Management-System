package com.softuni.event.controller;

import com.softuni.event.model.dto.UserProfileDTO;
import com.softuni.event.model.dto.UserRegisterDTO;
import com.softuni.event.service.NotificationService;
import com.softuni.event.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
        
        // Check if passwords match
        if (!userRegisterDTO.passwordsMatch()) {
            bindingResult.rejectValue("confirmPassword", "error.user", "Passwords do not match");
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
        return "redirect:/users/login?registered=true";
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

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        // Perform logout
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // Redirect to home page
        return "redirect:/";
    }

    @GetMapping("/admin/edit/{id}")
    public String adminEditUserForm(@PathVariable Long id, Model model) {
        try {
            UserProfileDTO userProfileDTO = userService.getUserById(id);
            model.addAttribute("userForm", userProfileDTO);
            return "admin-edit-user";
        } catch (Exception e) {
            return "redirect:/users/admin/users?error=User+not+found";
        }
    }
    
    @PostMapping("/admin/edit/{id}")
    public String adminUpdateUser(@PathVariable Long id, 
                                @Valid @ModelAttribute("userForm") UserProfileDTO userProfileDTO,
                                BindingResult bindingResult,
                                Model model) {
        // Set the ID to ensure we update the correct user
        userProfileDTO.setId(id);
        
        if (bindingResult.hasErrors()) {
            return "admin-edit-user";
        }
        
        try {
            userService.updateUserProfile(userProfileDTO);
            return "redirect:/users/admin/users?success=User+updated+successfully";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating user: " + e.getMessage());
            return "admin-edit-user";
        }
    }
}