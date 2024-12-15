package com.example.kitchensink.integration;

import com.example.kitchensink.dto.LoginRequest;
import com.example.kitchensink.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("rawtypes")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrorHandlingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testValidationErrors() {
        RegisterRequest invalidRequest = new RegisterRequest();
        // Empty fields should trigger validation
        invalidRequest.setUsername("");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("123"); // Too short

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/auth/register",
            invalidRequest,
            Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().get("success").toString().equals("true"));
        assertTrue(response.getBody().get("message").toString().contains("validation"));
    }

    @Test
    void testResourceNotFound() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/api/members/nonexistent-id",
            Map.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().get("success").toString().equals("true"));
    }

    @Test
    void testMethodNotAllowed() {
        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/auth/login",
            HttpMethod.PUT,
            null,
            Map.class
        );

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }

    @Test
    void testInvalidJsonFormat() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{invalid-json}", headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/auth/login",
            request,
            Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDuplicateResourceCreation() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("duplicate");
        request.setEmail("duplicate@example.com");
        request.setPassword("password123");

        // First registration
        restTemplate.postForEntity("/api/auth/register", request, Map.class);

        // Second registration with same username
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/auth/register",
            request,
            Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("already"));
    }

    @Test
    void testUnauthorizedAccess() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/api/members",
            Map.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("wrongpass");

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/auth/login",
            request,
            Map.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testMalformedJwtToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("malformed.jwt.token");

        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/members",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
} 