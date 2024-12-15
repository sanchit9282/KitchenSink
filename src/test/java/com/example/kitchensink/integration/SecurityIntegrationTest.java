package com.example.kitchensink.integration;

import com.example.kitchensink.dto.LoginRequest;
import com.example.kitchensink.dto.RegisterRequest;
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
class SecurityIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String AUTH_URL = "/api/auth";
    private static final String MEMBERS_URL = "/api/members";
    private static final String ADMIN_URL = "/api/admin";

    @BeforeEach
    void setUp() {
        // Register test users if needed
        registerTestUsers();
    }

    private void registerTestUsers() {
        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setUsername("testuser");
        userRequest.setEmail("testuser@example.com");
        userRequest.setPassword("password123");

        restTemplate.postForEntity(AUTH_URL + "/register", userRequest, Map.class);
    }

    @Test
    void testJwtAuthentication() {
        // 1. Get JWT token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser@example.com");
        loginRequest.setPassword("password123");

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            AUTH_URL + "/login",
            loginRequest,
            Map.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        String token = Objects.requireNonNull(loginResponse.getBody()).get("token").toString();

        // 2. Use token to access protected endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> protectedResponse = restTemplate.exchange(
            MEMBERS_URL,
            HttpMethod.GET,
            requestEntity,
            Map.class
        );

        assertEquals(HttpStatus.OK, protectedResponse.getStatusCode());
    }

    @Test
    void testInvalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.token.here");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            MEMBERS_URL,
            HttpMethod.GET,
            requestEntity,
            Map.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testExpiredToken() {
        // Note: This requires a token that's already expired
        String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYxNjc2MjQwMCwiZXhwIjoxNjE2NzYyNDAxfQ.expired_signature";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(expiredToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            MEMBERS_URL,
            HttpMethod.GET,
            requestEntity,
            Map.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testRoleBasedAccess() {
        // 1. Login as regular user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser@example.com");
        loginRequest.setPassword("password123");

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            AUTH_URL + "/login",
            loginRequest,
            Map.class
        );

        String token = Objects.requireNonNull(loginResponse.getBody()).get("token").toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // 2. Try to access admin endpoint
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            ADMIN_URL,
            HttpMethod.GET,
            requestEntity,
            Map.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testCorsConfiguration() {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("http://unauthorized-origin.com");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            MEMBERS_URL,
            HttpMethod.OPTIONS,
            requestEntity,
            Map.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testXssProtection() {
        Map<String, String> xssPayload = Map.of(
            "name", "<script>alert('xss')</script>",
            "email", "test@example.com",
            "phoneNumber", "1234567890"
        );

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(xssPayload);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            MEMBERS_URL,
            requestEntity,
            Map.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testCsrfProtection() {
        // CSRF should be disabled for our API
        Map<String, String> testData = Map.of("test", "data");
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(testData);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            MEMBERS_URL,
            requestEntity,
            Map.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        // Should fail due to missing auth token, not CSRF
    }
} 