package com.example.kitchensink.integration;

import com.example.kitchensink.dto.LoginRequest;
import com.example.kitchensink.dto.RegisterRequest;
import com.example.kitchensink.dto.TokenRefreshRequest;
import com.example.kitchensink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("rawtypes")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private static final String BASE_URL = "/api/auth";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void completeAuthenticationFlow() {
        // 1. Register new user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_USERNAME);
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);

        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
            BASE_URL + "/register",
            registerRequest,
            Map.class
        );

        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        assertTrue(Objects.requireNonNull(registerResponse.getBody())
            .get("message").toString().contains("registered successfully"));

        // 2. Login with registered user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_EMAIL); // Can login with email
        loginRequest.setPassword(TEST_PASSWORD);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            BASE_URL + "/login",
            loginRequest,
            Map.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        assertNotNull(loginResponse.getBody().get("token"));
        assertNotNull(loginResponse.getBody().get("refreshToken"));

        String accessToken = loginResponse.getBody().get("token").toString();
        String refreshToken = loginResponse.getBody().get("refreshToken").toString();

        // 3. Access protected endpoint with token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Map> protectedResponse = restTemplate.exchange(
            "/api/members",
            HttpMethod.GET,
            entity,
            Map.class
        );

        assertEquals(HttpStatus.OK, protectedResponse.getStatusCode());

        // 4. Refresh token
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        refreshRequest.setRefreshToken(refreshToken);

        ResponseEntity<Map> refreshResponse = restTemplate.postForEntity(
            BASE_URL + "/refresh-token",
            refreshRequest,
            Map.class
        );

        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
        assertNotNull(refreshResponse.getBody());
        assertNotNull(refreshResponse.getBody().get("accessToken"));

        // 5. Logout
        HttpEntity<TokenRefreshRequest> logoutEntity = new HttpEntity<>(refreshRequest, headers);
        ResponseEntity<Map> logoutResponse = restTemplate.postForEntity(
            BASE_URL + "/logout",
            logoutEntity,
            Map.class
        );

        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
    }

    @Test
    void registerWithExistingUsername() {
        // First registration
        RegisterRequest request = new RegisterRequest();
        request.setUsername(TEST_USERNAME);
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        restTemplate.postForEntity(BASE_URL + "/register", request, Map.class);

        // Second registration with same username
        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setUsername(TEST_USERNAME);
        duplicateRequest.setEmail("different@example.com");
        duplicateRequest.setPassword(TEST_PASSWORD);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            BASE_URL + "/register",
            duplicateRequest,
            Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody())
            .get("message").toString().contains("Username is already taken"));
    }

    @Test
    void loginWithInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("wrongpassword");

        ResponseEntity<Map> response = restTemplate.postForEntity(
            BASE_URL + "/login",
            request,
            Map.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void refreshTokenWithInvalidToken() {
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("invalid-refresh-token");

        ResponseEntity<Map> response = restTemplate.postForEntity(
            BASE_URL + "/refresh-token",
            request,
            Map.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void accessProtectedEndpointWithoutToken() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/members", Map.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
} 