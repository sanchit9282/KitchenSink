package com.example.kitchensink.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("rawtypes")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String authToken;

    @BeforeEach
    void setUp() {
        // Login and get token
        MultiValueMap<String, String> loginRequest = new LinkedMultiValueMap<>();
        loginRequest.add("username", "admin@example.com");
        loginRequest.add("password", "admin123");

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            "/api/auth/login",
            loginRequest,
            Map.class
        );

        authToken = "Bearer " + loginResponse.getBody().get("token").toString();
    }

    @Test
    void testApiVersioning() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.set("Api-Version", "1.0");

        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/members",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testContentNegotiation() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/members",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    void testResponseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/members",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        assertTrue(response.getHeaders().containsKey("X-Total-Count"));
        assertTrue(response.getHeaders().containsKey("Cache-Control"));
    }

    @Test
    void testPaginationHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/members?page=0&size=10",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        assertTrue(response.getHeaders().containsKey("X-Page-Number"));
        assertTrue(response.getHeaders().containsKey("X-Page-Size"));
        assertTrue(response.getHeaders().containsKey("X-Total-Pages"));
    }

    @Test
    void testCorsHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setOrigin("http://localhost:3000");

        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/members",
            HttpMethod.OPTIONS,
            new HttpEntity<>(headers),
            Map.class
        );

        assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Origin"));
        assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Methods"));
    }

    @Test
    void testRequestValidation() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> invalidRequest = Map.of(
            "name", "",  // Empty name should fail validation
            "email", "invalid-email",  // Invalid email format
            "phoneNumber", "123"  // Too short phone number
        );

        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/members",
            HttpMethod.POST,
            new HttpEntity<>(invalidRequest, headers),
            Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
} 