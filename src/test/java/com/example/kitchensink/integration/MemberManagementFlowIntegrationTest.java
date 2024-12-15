package com.example.kitchensink.integration;

import com.example.kitchensink.dto.LoginRequest;
import com.example.kitchensink.repository.MemberRepository;
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
class MemberManagementFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MemberRepository memberRepository;

    private static final String BASE_URL = "/api/members";
    private String authToken;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        // Login as admin to get token
        loginAsAdmin();
    }

    private void loginAsAdmin() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin@example.com");
        loginRequest.setPassword("admin123");

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/auth/login",
            loginRequest,
            Map.class
        );

        authToken = "Bearer " + Objects.requireNonNull(response.getBody()).get("token").toString();
    }

    @Test
    void memberManagementFlow() {
        // 1. Create a new member
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        Map<String, String> newMember = Map.of(
            "name", "John Doe",
            "email", "john@example.com",
            "phoneNumber", "1234567890"
        );

        HttpEntity<Map<String, String>> createRequest = new HttpEntity<>(newMember, headers);
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
            BASE_URL,
            createRequest,
            Map.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        
        // Get the created member ID
        @SuppressWarnings("unchecked")
        String memberId = ((Map<String, String>)createResponse.getBody().get("data")).get("id");

        // 2. Get member list
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        ResponseEntity<Map> listResponse = restTemplate.exchange(
            BASE_URL,
            HttpMethod.GET,
            getRequest,
            Map.class
        );

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        @SuppressWarnings("unchecked")
        boolean hasContent = ((Map<String, Object>)listResponse.getBody().get("data")).containsKey("content");
        assertTrue(hasContent);

        // 3. Update member
        Map<String, String> updateData = Map.of(
            "name", "John Updated",
            "phoneNumber", "0987654321"
        );

        HttpEntity<Map<String, String>> updateRequest = new HttpEntity<>(updateData, headers);
        ResponseEntity<Map> updateResponse = restTemplate.exchange(
            BASE_URL + "/" + memberId,
            HttpMethod.PUT,
            updateRequest,
            Map.class
        );

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        // 4. Delete member
        HttpEntity<Void> deleteRequest = new HttpEntity<>(headers);
        ResponseEntity<Map> deleteResponse = restTemplate.exchange(
            BASE_URL + "/" + memberId,
            HttpMethod.DELETE,
            deleteRequest,
            Map.class
        );

        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // Verify member is deleted
        assertFalse(memberRepository.existsById(memberId));
    }

    @Test
    void unauthorizedAccessTest() {
        // Try to access without token
        ResponseEntity<Map> response = restTemplate.getForEntity(BASE_URL, Map.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void invalidMemberDataTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        // Try to create member with invalid email
        Map<String, String> invalidMember = Map.of(
            "name", "John Doe",
            "email", "invalid-email",
            "phoneNumber", "1234567890"
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(invalidMember, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            BASE_URL,
            request,
            Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void duplicateEmailTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        Map<String, String> member = Map.of(
            "name", "John Doe",
            "email", "duplicate@example.com",
            "phoneNumber", "1234567890"
        );

        // Create first member
        HttpEntity<Map<String, String>> request = new HttpEntity<>(member, headers);
        restTemplate.postForEntity(BASE_URL, request, Map.class);

        // Try to create second member with same email
        ResponseEntity<Map> response = restTemplate.postForEntity(
            BASE_URL,
            request,
            Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
} 