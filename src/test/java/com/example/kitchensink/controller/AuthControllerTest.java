package com.example.kitchensink.controller;

import com.example.kitchensink.dto.LoginRequest;
import com.example.kitchensink.dto.RegisterRequest;
import com.example.kitchensink.dto.TokenRefreshRequest;
import com.example.kitchensink.model.RefreshToken;
import com.example.kitchensink.model.Role;
import com.example.kitchensink.model.User;
import com.example.kitchensink.repository.UserRepository;
import com.example.kitchensink.security.JwtUtils;
import com.example.kitchensink.security.UserDetailsImpl;
import com.example.kitchensink.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private User testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser@example.com");
        loginRequest.setPassword("password123");

        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");

        // Setup test user
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("encodedPassword");
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        testUser.setRoles(roles);

        // Setup refresh token
        testRefreshToken = new RefreshToken();
        testRefreshToken.setId("1");
        testRefreshToken.setUserId(testUser.getId());
        testRefreshToken.setToken("refresh-token");
    }

    @Test
    void whenValidLogin_thenReturnsToken() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(UserDetailsImpl.build(testUser));
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("test-jwt-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(testRefreshToken);

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);
    }

    @Test
    void whenValidRegistration_thenReturnsSuccess() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        ResponseEntity<?> response = authController.registerUser(registerRequest);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenDuplicateUsername_thenReturnsBadRequest() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(registerRequest);

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Error: Username is already taken!", response.getBody());
    }

    @Test
    void whenValidRefreshToken_thenReturnsNewAccessToken() {
        // Arrange
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        refreshRequest.setRefreshToken("valid-refresh-token");

        when(refreshTokenService.findByToken(refreshRequest.getRefreshToken()))
            .thenReturn(Optional.of(testRefreshToken));
        when(refreshTokenService.verifyExpiration(any(RefreshToken.class)))
            .thenReturn(testRefreshToken);
        when(userRepository.findById(testRefreshToken.getUserId()))
            .thenReturn(Optional.of(testUser));
        when(jwtUtils.generateTokenFromUsername(testUser.getUsername()))
            .thenReturn("new-access-token");

        // Act
        ResponseEntity<?> response = authController.refreshToken(refreshRequest);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(refreshTokenService).verifyExpiration(any(RefreshToken.class));
    }

    @Test
    void whenLogout_thenSuccessful() {
        // Arrange
        TokenRefreshRequest logoutRequest = new TokenRefreshRequest();
        logoutRequest.setRefreshToken("valid-refresh-token");

        when(refreshTokenService.findByToken(logoutRequest.getRefreshToken()))
            .thenReturn(Optional.of(testRefreshToken));

        // Act
        ResponseEntity<?> response = authController.logoutUser(logoutRequest);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(refreshTokenService).deleteByUserId(testRefreshToken.getUserId());
    }

    @Test
    void whenInvalidLogin_thenThrowsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authController.authenticateUser(loginRequest);
        });
        assertEquals("Invalid credentials", exception.getMessage());
    }
} 