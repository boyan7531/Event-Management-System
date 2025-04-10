package com.softuni.event.controller;

import com.softuni.event.model.dto.UserProfileDTO;
import com.softuni.event.model.dto.UserRegisterDTO;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.service.EventService;
import com.softuni.event.service.NotificationService;
import com.softuni.event.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private EventService eventService;

    private UserProfileDTO testUserProfileDTO;
    private UserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        testUserProfileDTO = new UserProfileDTO();
        testUserProfileDTO.setId(1L);
        testUserProfileDTO.setUsername("testuser");
        testUserProfileDTO.setEmail("test@example.com");
        testUserProfileDTO.setFirstName("Test");
        testUserProfileDTO.setLastName("User");

        testUserEntity = new UserEntity();
        testUserEntity.setId(1L);
        testUserEntity.setUsername("testuser");
    }

    @Test
    void testRegisterForm() throws Exception {
        mockMvc.perform(get("/users/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userRegisterDTO"));
    }

    @Test
    void testRegister_Success() throws Exception {
        when(userService.isUsernameExists(anyString())).thenReturn(false);
        when(userService.isEmailExists(anyString())).thenReturn(false);
        doNothing().when(userService).registerUser(any());

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "newuser")
                        .param("email", "new@example.com")
                        .param("firstName", "New")
                        .param("lastName", "User")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login?registered=true"));

        verify(userService, times(1)).registerUser(any());
    }

    @Test
    void testRegister_ValidationError() throws Exception {
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "") // Invalid
                        .param("email", "new@example.com")
                        .param("firstName", "New")
                        .param("lastName", "User")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("userRegisterDTO", "username"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void testRegister_PasswordMismatch() throws Exception {
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "newuser")
                        .param("email", "new@example.com")
                        .param("firstName", "New")
                        .param("lastName", "User")
                        .param("password", "password123")
                        .param("confirmPassword", "password456") // Mismatch
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("userRegisterDTO", "confirmPassword"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void testRegister_UsernameExists() throws Exception {
        when(userService.isUsernameExists("existinguser")).thenReturn(true);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "existinguser") // Existing
                        .param("email", "new@example.com")
                        .param("firstName", "New")
                        .param("lastName", "User")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("userRegisterDTO", "username"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void testLoginForm() throws Exception {
        mockMvc.perform(get("/users/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testProfile() throws Exception {
        when(userService.getUserProfile("testuser")).thenReturn(testUserProfileDTO);
        when(userService.getUserByUsername("testuser")).thenReturn(testUserEntity);
        when(notificationService.getNotificationsForUser(testUserEntity)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", hasProperty("username", is("testuser"))))
                .andExpect(model().attributeExists("notifications"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testEditProfileForm() throws Exception {
        when(userService.getUserProfile("testuser")).thenReturn(testUserProfileDTO);

        mockMvc.perform(get("/users/profile/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-profile"))
                .andExpect(model().attributeExists("userProfileDTO"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testUpdateProfile() throws Exception {
        doNothing().when(userService).updateUserProfile(any());

        mockMvc.perform(post("/users/profile/edit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "1")
                        .param("username", "testuser")
                        .param("email", "updated@example.com")
                        .param("firstName", "Updated")
                        .param("lastName", "User")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/profile"));

        verify(userService, times(1)).updateUserProfile(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminUsersList() throws Exception {
        List<UserProfileDTO> userList = Arrays.asList(testUserProfileDTO);
        when(userService.getAllUsers()).thenReturn(userList);
        when(eventService.getEventsByStatus(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("pendingEvents"))
                .andExpect(model().attributeExists("approvedEvents"))
                .andExpect(model().attributeExists("rejectedEvents"))
                .andExpect(model().attributeExists("canceledEvents"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testChangeUserRole() throws Exception {
        doNothing().when(userService).changeUserRole(anyLong(), anyString());

        mockMvc.perform(post("/users/admin/users/1/change-role")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("role", "ADMIN")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/admin/users"));

        verify(userService, times(1)).changeUserRole(1L, "ADMIN");
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(get("/users/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminEditUserForm() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUserProfileDTO);

        mockMvc.perform(get("/users/admin/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-edit-user"))
                .andExpect(model().attributeExists("userForm"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN") 
    void testAdminUpdateUser_Success() throws Exception {
        doNothing().when(userService).updateUserProfile(any());

        mockMvc.perform(post("/users/admin/edit/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "1")
                        .param("username", "testuser")
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/admin/users?success=User+updated+successfully"));

        verify(userService, times(1)).updateUserProfile(any());
    }
} 