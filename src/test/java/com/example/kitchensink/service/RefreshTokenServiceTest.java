package com.example.kitchensink.service;

import com.example.kitchensink.model.RefreshToken;
import com.example.kitchensink.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private RefreshToken testRefreshToken;
    private final String TEST_USER_ID = "test-user-id";

    @BeforeEach
    void setUp() {
        // Set the refresh token duration (15 minutes in milliseconds)
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 900000L);

        testRefreshToken = new RefreshToken();
        testRefreshToken.setId("test-token-id");
        testRefreshToken.setUserId(TEST_USER_ID);
        testRefreshToken.setToken("test-refresh-token");
        testRefreshToken.setExpiryDate(Instant.now().plusMillis(900000)); // 15 minutes from now
    }

    @Test
    void whenCreateRefreshToken_thenSuccess() {
        // Arrange
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);
        when(refreshTokenRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken(TEST_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        assertNotNull(result.getToken());
        assertNotNull(result.getExpiryDate());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
    }

    @Test
    void whenCreateRefreshToken_withExistingToken_thenDeleteOldToken() {
        // Arrange
        RefreshToken oldToken = new RefreshToken();
        oldToken.setId("old-token-id");
        oldToken.setUserId(TEST_USER_ID);
        
        when(refreshTokenRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        // Act
        refreshTokenService.createRefreshToken(TEST_USER_ID);

        // Assert
        verify(refreshTokenRepository).delete(oldToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void whenVerifyValidToken_thenSuccess() {
        // Act
        RefreshToken result = refreshTokenService.verifyExpiration(testRefreshToken);

        // Assert
        assertNotNull(result);
        assertEquals(testRefreshToken.getToken(), result.getToken());
    }

    @Test
    void whenVerifyExpiredToken_thenThrowsException() {
        // Arrange
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiryDate(Instant.now().minusSeconds(300)); // 5 minutes ago

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            refreshTokenService.verifyExpiration(expiredToken);
        });
        
        assertEquals("Refresh token was expired", exception.getMessage());
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    void whenFindByValidToken_thenReturnsToken() {
        // Arrange
        when(refreshTokenRepository.findByToken(testRefreshToken.getToken()))
            .thenReturn(Optional.of(testRefreshToken));

        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken(testRefreshToken.getToken());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRefreshToken.getToken(), result.get().getToken());
    }

    @Test
    void whenFindByInvalidToken_thenReturnsEmpty() {
        // Arrange
        when(refreshTokenRepository.findByToken("invalid-token"))
            .thenReturn(Optional.empty());

        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken("invalid-token");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void whenDeleteByUserId_thenSuccess() {
        // Act
        refreshTokenService.deleteByUserId(TEST_USER_ID);

        // Assert
        verify(refreshTokenRepository).deleteByUserId(TEST_USER_ID);
    }

    @Test
    void whenTokenExpirationCalculated_thenCorrectTimeSet() {
        // Act
        RefreshToken token = refreshTokenService.createRefreshToken(TEST_USER_ID);

        // Assert
        Instant expectedExpiry = Instant.now().plusMillis(900000); // 15 minutes
        assertTrue(token.getExpiryDate().isBefore(expectedExpiry.plusSeconds(1)));
        assertTrue(token.getExpiryDate().isAfter(expectedExpiry.minusSeconds(1)));
    }
} 