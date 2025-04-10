package com.softuni.event.service.impl;

import com.softuni.event.model.dto.UserProfileDTO;
import com.softuni.event.model.dto.UserRegisterDTO;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.entity.UserRoleEntity;
import com.softuni.event.model.enums.UserRole;
import com.softuni.event.repository.UserRepository;
import com.softuni.event.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<UserEntity> userCaptor;

    @Captor
    private ArgumentCaptor<UserRoleEntity> roleCaptor;

    private UserEntity testUser;
    private UserRegisterDTO testRegisterDTO;
    private UserProfileDTO testProfileDTO;
    private UserRoleEntity userRole;
    private UserRoleEntity adminRole;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhone("1234567890");
        testUser.setCreatedAt(LocalDateTime.now().minusDays(1));
        testUser.setRoles(new HashSet<>());
        testUser.setOrganizedEvents(new HashSet<>());
        testUser.setAttendedEvents(new HashSet<>());
        testUser.setTickets(new HashSet<>());
        testUser.setPayments(new HashSet<>());

        testRegisterDTO = new UserRegisterDTO();
        testRegisterDTO.setUsername("newuser");
        testRegisterDTO.setEmail("new@example.com");
        testRegisterDTO.setPassword("password123");
        testRegisterDTO.setFirstName("New");
        testRegisterDTO.setLastName("User");
        testRegisterDTO.setPhone("0987654321");

        testProfileDTO = new UserProfileDTO();
        testProfileDTO.setId(1L);
        testProfileDTO.setUsername("testuser");
        testProfileDTO.setEmail("test@example.com");
        testProfileDTO.setFirstName("Test");
        testProfileDTO.setLastName("User");
        testProfileDTO.setPhone("1234567890");
        testProfileDTO.setAdmin(false);

        userRole = new UserRoleEntity();
        userRole.setId(1L);
        userRole.setRole(UserRole.USER);
        userRole.setUser(testUser);

        adminRole = new UserRoleEntity();
        adminRole.setId(2L);
        adminRole.setRole(UserRole.ADMIN);
        adminRole.setUser(testUser);
    }

    @Test
    void registerUser_ShouldCreateUserWithUserRole() {
        // Arrange
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);
        when(userRoleRepository.findByRole(UserRole.USER)).thenReturn(Collections.emptyList());
        when(userRoleRepository.save(any(UserRoleEntity.class))).thenReturn(userRole);

        // Act
        userService.registerUser(testRegisterDTO);

        // Assert
        verify(passwordEncoder).encode("password123");
        verify(userRepository, times(2)).save(userCaptor.capture());
        verify(userRoleRepository).findByRole(UserRole.USER);
        verify(userRoleRepository).save(roleCaptor.capture());

        List<UserEntity> capturedUsers = userCaptor.getAllValues();
        UserEntity capturedUser = capturedUsers.get(0); // Get first saved user
        assertEquals("newuser", capturedUser.getUsername());
        assertEquals("new@example.com", capturedUser.getEmail());
        assertEquals("encodedPassword", capturedUser.getPassword());
        assertEquals("New", capturedUser.getFirstName());
        assertEquals("User", capturedUser.getLastName());
        assertEquals("0987654321", capturedUser.getPhone());
        assertNotNull(capturedUser.getCreatedAt());

        UserRoleEntity capturedRole = roleCaptor.getValue();
        assertEquals(UserRole.USER, capturedRole.getRole());
        assertNotNull(capturedRole.getUser());
    }

    @Test
    void registerUser_ShouldUseExistingUserRole() {
        // Arrange
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);
        when(userRoleRepository.findByRole(UserRole.USER)).thenReturn(List.of(userRole));
        when(userRoleRepository.save(any(UserRoleEntity.class))).thenReturn(userRole);

        // Act
        userService.registerUser(testRegisterDTO);

        // Assert
        verify(passwordEncoder).encode("password123");
        verify(userRepository, times(2)).save(userCaptor.capture());
        verify(userRoleRepository).findByRole(UserRole.USER);
        verify(userRoleRepository).save(roleCaptor.capture());

        UserRoleEntity capturedRole = roleCaptor.getValue();
        assertEquals(UserRole.USER, capturedRole.getRole());
        assertNotNull(capturedRole.getUser());
    }

    @Test
    void getUserProfile_ShouldReturnProfileDTO() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, UserProfileDTO.class)).thenReturn(testProfileDTO);

        // Act
        UserProfileDTO result = userService.getUserProfile("testuser");

        // Assert
        assertEquals(testProfileDTO, result);
        assertFalse(result.isAdmin());
        verify(userRepository).findByUsername("testuser");
        verify(modelMapper).map(testUser, UserProfileDTO.class);
    }

    @Test
    void getUserProfile_ShouldSetAdminFlag() {
        // Arrange
        testUser.getRoles().add(adminRole);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, UserProfileDTO.class)).thenReturn(testProfileDTO);

        // Act
        UserProfileDTO result = userService.getUserProfile("testuser");

        // Assert
        assertTrue(result.isAdmin());
        verify(userRepository).findByUsername("testuser");
        verify(modelMapper).map(testUser, UserProfileDTO.class);
    }

    @Test
    void getUserProfile_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserProfile("unknownuser");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername("unknownuser");
        verifyNoInteractions(modelMapper);
    }

    @Test
    void getUserById_ShouldReturnProfileDTO() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, UserProfileDTO.class)).thenReturn(testProfileDTO);

        // Act
        UserProfileDTO result = userService.getUserById(1L);

        // Assert
        assertEquals(testProfileDTO, result);
        assertFalse(result.isAdmin());
        verify(userRepository).findById(1L);
        verify(modelMapper).map(testUser, UserProfileDTO.class);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(999L);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(999L);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void updateUserProfile_ShouldUpdateUser() {
        // Arrange
        testProfileDTO.setFirstName("Updated");
        testProfileDTO.setLastName("Name");
        testProfileDTO.setPhone("5555555555");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userService.updateUserProfile(testProfileDTO);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).save(userCaptor.capture());
        
        UserEntity capturedUser = userCaptor.getValue();
        assertEquals("testuser", capturedUser.getUsername());
        assertEquals("test@example.com", capturedUser.getEmail());
        assertEquals("Updated", capturedUser.getFirstName());
        assertEquals("Name", capturedUser.getLastName());
        assertEquals("5555555555", capturedUser.getPhone());
        assertNotNull(capturedUser.getUpdatedAt());
    }

    @Test
    void updateUserProfile_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        testProfileDTO.setId(999L);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserProfile(testProfileDTO);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(999L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void isUsernameExists_ShouldReturnTrue_WhenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean result = userService.isUsernameExists("testuser");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void isUsernameExists_ShouldReturnFalse_WhenUsernameDoesNotExist() {
        // Arrange
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // Act
        boolean result = userService.isUsernameExists("nonexistent");

        // Assert
        assertFalse(result);
        verify(userRepository).existsByUsername("nonexistent");
    }

    @Test
    void isEmailExists_ShouldReturnTrue_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean result = userService.isEmailExists("test@example.com");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void isEmailExists_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // Arrange
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // Act
        boolean result = userService.isEmailExists("nonexistent@example.com");

        // Assert
        assertFalse(result);
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Arrange
        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setRoles(new HashSet<>(Collections.singletonList(adminRole)));

        List<UserEntity> users = Arrays.asList(testUser, user2);
        
        UserProfileDTO user2DTO = new UserProfileDTO();
        user2DTO.setId(2L);
        user2DTO.setUsername("user2");
        
        when(userRepository.findAll()).thenReturn(users);
        when(modelMapper.map(eq(testUser), eq(UserProfileDTO.class))).thenReturn(testProfileDTO);
        when(modelMapper.map(eq(user2), eq(UserProfileDTO.class))).thenReturn(user2DTO);

        // Act
        List<UserProfileDTO> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals(testProfileDTO, result.get(0));
        assertEquals(user2DTO, result.get(1));
        assertTrue(result.get(1).isAdmin());
        verify(userRepository).findAll();
        verify(modelMapper, times(2)).map(any(UserEntity.class), eq(UserProfileDTO.class));
    }

    @Test
    void changeUserRole_ShouldAddAdminRole() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Use thenAnswer to modify the testUser object when saving a role
        when(userRoleRepository.save(any(UserRoleEntity.class))).thenAnswer(invocation -> {
            UserRoleEntity savedRole = invocation.getArgument(0);
            savedRole.setId(2L); // Set role ID
            return savedRole;
        });
        
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userService.changeUserRole(1L, "ADMIN");

        // Assert
        verify(userRepository).findById(1L);
        verify(userRoleRepository).save(roleCaptor.capture());
        verify(userRepository).save(userCaptor.capture());
        
        UserRoleEntity capturedRole = roleCaptor.getValue();
        assertEquals(UserRole.ADMIN, capturedRole.getRole());
        assertEquals(testUser, capturedRole.getUser());
        
        // Check if user has a role with ADMIN role type (not checking object identity)
        boolean hasAdminRole = userCaptor.getValue().getRoles().stream()
            .anyMatch(role -> role.getRole() == UserRole.ADMIN);
        assertTrue(hasAdminRole, "User should have ADMIN role");
    }

    @Test
    void changeUserRole_ShouldRemoveAdminRole() {
        // Arrange
        testUser.getRoles().add(adminRole);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRoleRepository).delete(any(UserRoleEntity.class));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userService.changeUserRole(2L, "USER");

        // Assert
        verify(userRepository).findById(2L);
        verify(userRoleRepository).delete(any(UserRoleEntity.class));
        verify(userRepository).save(userCaptor.capture());
        
        UserEntity capturedUser = userCaptor.getValue();
        assertFalse(capturedUser.getRoles().contains(adminRole));
    }

    @Test
    void changeUserRole_ShouldThrowException_WhenRemovingAdminRoleFromSuperAdmin() {
        // Arrange
        testUser.getRoles().add(adminRole);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            userService.changeUserRole(1L, "USER");
        });

        assertEquals("Cannot remove ADMIN role from the super administrator", exception.getMessage());
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository, userRoleRepository);
    }

    @Test
    void changeUserRole_ShouldNotAddAdminRoleIfAlreadyExists() {
        // Arrange
        testUser.getRoles().add(adminRole);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userService.changeUserRole(1L, "ADMIN");

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        verifyNoInteractions(userRoleRepository);
    }

    @Test
    void changeUserRole_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changeUserRole(999L, "ADMIN");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(999L);
        verifyNoMoreInteractions(userRepository, userRoleRepository);
    }

    @Test
    void getUserByUsername_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserEntity result = userService.getUserByUsername("testuser");

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserByUsername("unknownuser");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername("unknownuser");
    }

    @Test
    void getAllAdmins_ShouldReturnOnlyAdmins() {
        // Arrange
        UserEntity adminUser = new UserEntity();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRoles(new HashSet<>(Collections.singletonList(adminRole)));
        
        List<UserEntity> allUsers = Arrays.asList(testUser, adminUser);
        when(userRepository.findAll()).thenReturn(allUsers);

        // Act
        List<UserEntity> result = userService.getAllAdmins();

        // Assert
        assertEquals(1, result.size());
        assertEquals(adminUser, result.get(0));
        verify(userRepository).findAll();
    }
} 