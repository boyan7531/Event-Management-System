package com.softuni.event.repository;

import com.softuni.event.model.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    
    private UserEntity user1;
    private UserEntity user2;
    
    @BeforeEach
    void setUp() {
        // Clean up any existing users
        userRepository.deleteAll();
        
        // Create test users
        user1 = new UserEntity();
        user1.setUsername("johndoe");
        user1.setEmail("john.doe@example.com");
        user1.setPassword("password123");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setCreatedAt(LocalDateTime.now());
        user1 = userRepository.save(user1);
        
        user2 = new UserEntity();
        user2.setUsername("janesmith");
        user2.setEmail("jane.smith@example.com");
        user2.setPassword("password456");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setPhone("555-1234");
        user2.setCreatedAt(LocalDateTime.now());
        user2 = userRepository.save(user2);
    }
    
    @Test
    void testFindByUsername() {
        // Act
        Optional<UserEntity> foundUser1 = userRepository.findByUsername("johndoe");
        Optional<UserEntity> foundUser2 = userRepository.findByUsername("janesmith");
        Optional<UserEntity> nonExistentUser = userRepository.findByUsername("nonexistent");
        
        // Assert
        assertTrue(foundUser1.isPresent());
        assertEquals(user1.getId(), foundUser1.get().getId());
        assertEquals("John", foundUser1.get().getFirstName());
        assertEquals("Doe", foundUser1.get().getLastName());
        
        assertTrue(foundUser2.isPresent());
        assertEquals(user2.getId(), foundUser2.get().getId());
        assertEquals("Jane", foundUser2.get().getFirstName());
        assertEquals("Smith", foundUser2.get().getLastName());
        
        assertFalse(nonExistentUser.isPresent());
    }
    
    @Test
    void testFindByEmail() {
        // Act
        Optional<UserEntity> foundUser1 = userRepository.findByEmail("john.doe@example.com");
        Optional<UserEntity> foundUser2 = userRepository.findByEmail("jane.smith@example.com");
        Optional<UserEntity> nonExistentUser = userRepository.findByEmail("nonexistent@example.com");
        
        // Assert
        assertTrue(foundUser1.isPresent());
        assertEquals(user1.getId(), foundUser1.get().getId());
        assertEquals("johndoe", foundUser1.get().getUsername());
        
        assertTrue(foundUser2.isPresent());
        assertEquals(user2.getId(), foundUser2.get().getId());
        assertEquals("janesmith", foundUser2.get().getUsername());
        
        assertFalse(nonExistentUser.isPresent());
    }
    
    @Test
    void testExistsByUsername() {
        // Act
        boolean user1Exists = userRepository.existsByUsername("johndoe");
        boolean user2Exists = userRepository.existsByUsername("janesmith");
        boolean nonExistentUserExists = userRepository.existsByUsername("nonexistent");
        
        // Assert
        assertTrue(user1Exists);
        assertTrue(user2Exists);
        assertFalse(nonExistentUserExists);
    }
    
    @Test
    void testExistsByEmail() {
        // Act
        boolean user1Exists = userRepository.existsByEmail("john.doe@example.com");
        boolean user2Exists = userRepository.existsByEmail("jane.smith@example.com");
        boolean nonExistentUserExists = userRepository.existsByEmail("nonexistent@example.com");
        
        // Assert
        assertTrue(user1Exists);
        assertTrue(user2Exists);
        assertFalse(nonExistentUserExists);
    }
    
    @Test
    void testBasicJpaRepositoryMethods() {
        // Test findById
        Optional<UserEntity> foundUser = userRepository.findById(user1.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("johndoe", foundUser.get().getUsername());
        
        // Test findAll
        List<UserEntity> allUsers = userRepository.findAll();
        assertEquals(2, allUsers.size());
        
        // Test update
        UserEntity userToUpdate = foundUser.get();
        userToUpdate.setFirstName("Jonathan");
        userToUpdate.setPhone("555-5678");
        userRepository.save(userToUpdate);
        
        Optional<UserEntity> updatedUser = userRepository.findById(user1.getId());
        assertTrue(updatedUser.isPresent());
        assertEquals("Jonathan", updatedUser.get().getFirstName());
        assertEquals("555-5678", updatedUser.get().getPhone());
        
        // Test delete
        userRepository.delete(userToUpdate);
        assertEquals(1, userRepository.count());
        assertFalse(userRepository.findById(user1.getId()).isPresent());
    }
    
    @Test
    void testCaseSensitivity() {
        // Act - testing case sensitivity for username
        Optional<UserEntity> upperCaseUsername = userRepository.findByUsername("JOHNDOE");
        Optional<UserEntity> mixedCaseUsername = userRepository.findByUsername("JohnDoe");
        
        // Act - testing case sensitivity for email
        Optional<UserEntity> upperCaseEmail = userRepository.findByEmail("JOHN.DOE@EXAMPLE.COM");
        Optional<UserEntity> mixedCaseEmail = userRepository.findByEmail("John.Doe@Example.com");
        
        // Assert - username queries are case-sensitive (H2 default behavior)
        assertFalse(upperCaseUsername.isPresent());
        assertFalse(mixedCaseUsername.isPresent());
        
        // Assert - email queries are case-sensitive (H2 default behavior)
        assertFalse(upperCaseEmail.isPresent());
        assertFalse(mixedCaseEmail.isPresent());
    }
    
    @Test
    void testUserWithNullableFields() {
        // Create user with nullable fields
        UserEntity userWithNulls = new UserEntity();
        userWithNulls.setUsername("userwithnulls");
        userWithNulls.setEmail("nulls@example.com");
        userWithNulls.setPassword("password");
        userWithNulls.setFirstName(null);  // Nullable field
        userWithNulls.setLastName(null);   // Nullable field
        userWithNulls.setPhone(null);      // Nullable field
        userWithNulls.setCreatedAt(LocalDateTime.now());
        
        // Save and retrieve
        UserEntity savedUser = userRepository.save(userWithNulls);
        Optional<UserEntity> retrievedUser = userRepository.findById(savedUser.getId());
        
        // Assert
        assertTrue(retrievedUser.isPresent());
        assertNull(retrievedUser.get().getFirstName());
        assertNull(retrievedUser.get().getLastName());
        assertNull(retrievedUser.get().getPhone());
    }
} 