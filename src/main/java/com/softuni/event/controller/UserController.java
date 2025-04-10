package com.softuni.event.controller;

import com.softuni.event.model.dto.UserProfileDTO;
import com.softuni.event.model.dto.UserRegisterDTO;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.service.EventService;
import com.softuni.event.service.NotificationService;
import com.softuni.event.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final EventService eventService;

    public UserController(UserService userService, 
                         PasswordEncoder passwordEncoder,
                         NotificationService notificationService,
                         EventService eventService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.eventService = eventService;
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

    @GetMapping("/admin-login")
    public String adminLogin(HttpServletRequest request) {
        try {
            // This is just for demo purposes - in a real app, you would have better security
            request.login("admin", System.getProperty("ADMIN_PASSWORD", "adminpass"));
            return "redirect:/users/admin/users";
        } catch (Exception e) {
            return "redirect:/users/login?error";
        }
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserProfileDTO userProfile = userService.getUserProfile(userDetails.getUsername());
        model.addAttribute("user", userProfile);
        
        // Only show unread notifications
        UserEntity user = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("notifications", 
                notificationService.getUnreadNotificationsForUser(user));
        
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
        
        // Add events categorized by status for admin dashboard
        model.addAttribute("pendingEvents", eventService.getEventsByStatus(EventStatus.PENDING));
        model.addAttribute("approvedEvents", eventService.getEventsByStatus(EventStatus.APPROVED));
        model.addAttribute("rejectedEvents", eventService.getEventsByStatus(EventStatus.REJECTED));
        model.addAttribute("canceledEvents", eventService.getEventsByStatus(EventStatus.CANCELED));
        
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