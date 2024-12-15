package com.example.kitchensink.service;

import com.example.kitchensink.model.Role;
import com.example.kitchensink.model.User;
import com.example.kitchensink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DataMigrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DataMigrationService dataMigrationService;

    private User userWithoutAdminRole;
    private User userWithAdminRole;

    @BeforeEach
    void setUp() {
        // Setup user without admin role
        userWithoutAdminRole = new User();
        userWithoutAdminRole.setId("1");
        userWithoutAdminRole.setUsername("user1");
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(Role.ROLE_USER);
        userWithoutAdminRole.setRoles(userRoles);

        // Setup user with admin role
        userWithAdminRole = new User();
        userWithAdminRole.setId("2");
        userWithAdminRole.setUsername("admin1");
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(Role.ROLE_USER);
        adminRoles.add(Role.ROLE_ADMIN);
        userWithAdminRole.setRoles(adminRoles);
    }

    @Test
    void whenMigrateUserRoles_thenAddAdminRoleToNonAdminUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(userWithoutAdminRole));
        when(userRepository.save(any(User.class))).thenReturn(userWithoutAdminRole);

        // Act
        dataMigrationService.migrateUserRoles();

        // Assert
        verify(userRepository).save(any(User.class));
        assertTrue(userWithoutAdminRole.getRoles().contains(Role.ROLE_ADMIN));
    }

    @Test
    void whenMigrateUserRoles_thenSkipUsersWithAdminRole() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(userWithAdminRole));

        // Act
        dataMigrationService.migrateUserRoles();

        // Assert
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenMigrateUserRoles_withMultipleUsers_thenProcessAll() {
        // Arrange
        List<User> users = Arrays.asList(userWithoutAdminRole, userWithAdminRole);
        when(userRepository.findAll()).thenReturn(users);
        when(userRepository.save(any(User.class))).thenReturn(userWithoutAdminRole);

        // Act
        dataMigrationService.migrateUserRoles();

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void whenMigrateUserRoles_withEmptyUserList_thenNoAction() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        dataMigrationService.migrateUserRoles();

        // Assert
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenMigrateUserRoles_withDatabaseError_thenHandleGracefully() {
        // Arrange
        when(userRepository.findAll()).thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertDoesNotThrow(() -> dataMigrationService.migrateUserRoles());
    }

    @Test
    void whenMigrateUserRoles_withSaveError_thenContinueProcessing() {
        // Arrange
        User user1 = createUserWithoutAdminRole("3", "user3");
        User user2 = createUserWithoutAdminRole("4", "user4");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(userRepository.save(user1)).thenThrow(new DataAccessException("Save error") {});
        when(userRepository.save(user2)).thenReturn(user2);

        // Act
        dataMigrationService.migrateUserRoles();

        // Assert
        verify(userRepository).save(user1); // First save fails
        verify(userRepository).save(user2); // Second save succeeds
    }

    private User createUserWithoutAdminRole(String id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        user.setRoles(roles);
        return user;
    }

    @Test
    void whenMigrateUserRoles_withNullRoles_thenInitializeRoles() {
        // Arrange
        User userWithNullRoles = new User();
        userWithNullRoles.setId("5");
        userWithNullRoles.setUsername("user5");
        userWithNullRoles.setRoles(null);
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(userWithNullRoles));
        when(userRepository.save(any(User.class))).thenReturn(userWithNullRoles);

        // Act
        dataMigrationService.migrateUserRoles();

        // Assert
        verify(userRepository).save(any(User.class));
        assertNotNull(userWithNullRoles.getRoles());
        assertTrue(userWithNullRoles.getRoles().contains(Role.ROLE_ADMIN));
    }
} 