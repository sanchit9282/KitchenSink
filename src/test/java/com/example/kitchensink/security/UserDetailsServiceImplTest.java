package com.example.kitchensink.security;

import com.example.kitchensink.model.Role;
import com.example.kitchensink.model.User;
import com.example.kitchensink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        testUser.setRoles(roles);
    }

    @Test
    void whenLoadByUsername_thenSuccess() {
        // Arrange
        when(userRepository.findByUsername(testUser.getUsername()))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());

        // Assert
        assertNotNull(userDetails);
        assertEquals(testUser.getUsername(), userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().contains(
            new SimpleGrantedAuthority(Role.ROLE_USER.name())
        ));
    }

    @Test
    void whenLoadByEmail_thenSuccess() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getEmail());

        // Assert
        assertNotNull(userDetails);
        assertEquals(testUser.getUsername(), userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().contains(
            new SimpleGrantedAuthority(Role.ROLE_USER.name())
        ));
    }

    @Test
    void whenUserNotFound_thenThrowException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void whenUserHasMultipleRoles_thenAllRolesLoaded() {
        // Arrange
        Set<Role> multipleRoles = new HashSet<>();
        multipleRoles.add(Role.ROLE_USER);
        multipleRoles.add(Role.ROLE_ADMIN);
        testUser.setRoles(multipleRoles);

        when(userRepository.findByUsername(testUser.getUsername()))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());

        // Assert
        assertNotNull(userDetails);
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(
            new SimpleGrantedAuthority(Role.ROLE_USER.name())
        ));
        assertTrue(userDetails.getAuthorities().contains(
            new SimpleGrantedAuthority(Role.ROLE_ADMIN.name())
        ));
    }

    @Test
    void whenUserFoundButNoRoles_thenEmptyAuthorities() {
        // Arrange
        testUser.setRoles(new HashSet<>());
        when(userRepository.findByUsername(testUser.getUsername()))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    void whenLoadByUsername_thenAccountNonExpired() {
        // Arrange
        when(userRepository.findByUsername(testUser.getUsername()))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());

        // Assert
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }
} 