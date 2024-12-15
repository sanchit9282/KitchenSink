package com.example.kitchensink.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    private UserDetailsImpl userDetails;
    private static final String TEST_JWT_SECRET = "======================BezKoder=Spring===========================";
    private static final int JWT_EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_JWT_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", JWT_EXPIRATION_MS);

        userDetails = new UserDetailsImpl(
            "1",
            "testuser",
            "test@example.com",
            "password",
            new HashSet<>()
        );
    }

    @Test
    void whenGenerateJwtToken_thenSuccess() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        String token = jwtUtils.generateJwtToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals(userDetails.getUsername(), jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    void whenGenerateTokenFromUsername_thenSuccess() {
        // Act
        String token = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        // Assert
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals(userDetails.getUsername(), jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    void whenTokenExpired_thenValidationFails() {
        // Arrange
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -3600000); // expired 1 hour ago
        String token = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    void whenInvalidToken_thenValidationFails() {
        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken("invalid.token.here"));
    }

    @Test
    void whenMalformedToken_thenValidationFails() {
        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken("malformed.token"));
    }

    @Test
    void whenEmptyToken_thenValidationFails() {
        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken(""));
    }

    @Test
    void whenNullToken_thenValidationFails() {
        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken(null));
    }

    @Test
    void whenExtractUsername_thenSuccess() {
        // Arrange
        String token = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        // Act
        String username = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertEquals(userDetails.getUsername(), username);
    }

    @Test
    void whenTokenWithInvalidSignature_thenValidationFails() {
        // Arrange
        String token = jwtUtils.generateTokenFromUsername(userDetails.getUsername());
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "different_secret_key_here");

        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    void whenTokenModified_thenValidationFails() {
        // Arrange
        String token = jwtUtils.generateTokenFromUsername(userDetails.getUsername());
        String modifiedToken = token.substring(0, token.length() - 5) + "modified";

        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken(modifiedToken));
    }

    @Test
    void whenValidateExpiredToken_thenThrowsException() {
        // Arrange
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 0); // Immediate expiration
        String token = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            Thread.sleep(1); // Ensure token expires
            jwtUtils.getUserNameFromJwtToken(token);
        });
    }

    @Test
    void whenValidateMalformedToken_thenThrowsException() {
        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtils.getUserNameFromJwtToken("malformed.token.here");
        });
    }
} 