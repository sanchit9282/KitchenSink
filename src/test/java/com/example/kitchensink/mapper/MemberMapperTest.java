package com.example.kitchensink.mapper;

import com.example.kitchensink.dto.CreateMemberRequest;
import com.example.kitchensink.dto.MemberDto;
import com.example.kitchensink.dto.UpdateMemberRequest;
import com.example.kitchensink.model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberMapperTest {

    private MemberMapper memberMapper;
    private Member testMember;
    private MemberDto testMemberDto;
    private CreateMemberRequest createRequest;
    private UpdateMemberRequest updateRequest;

    @BeforeEach
    void setUp() {
        memberMapper = new MemberMapper();

        // Setup test member
        testMember = new Member();
        testMember.setId("1");
        testMember.setName("Test User");
        testMember.setEmail("test@example.com");
        testMember.setPhoneNumber("+1-555-0123");

        // Setup test DTO
        testMemberDto = new MemberDto();
        testMemberDto.setId("1");
        testMemberDto.setName("Test User");
        testMemberDto.setEmail("test@example.com");
        testMemberDto.setPhoneNumber("+1-555-0123");

        // Setup create request
        createRequest = new CreateMemberRequest();
        createRequest.setName("New User");
        createRequest.setEmail("new@example.com");
        createRequest.setPhoneNumber("+1-555-0124");

        // Setup update request
        updateRequest = new UpdateMemberRequest();
        updateRequest.setName("Updated User");
        updateRequest.setPhoneNumber("+1-555-0125");
    }

    @Test
    void whenMapEntityToDto_thenAllFieldsMapped() {
        // Act
        MemberDto result = memberMapper.toDto(testMember);

        // Assert
        assertNotNull(result);
        assertEquals(testMember.getId(), result.getId());
        assertEquals(testMember.getName(), result.getName());
        assertEquals(testMember.getEmail(), result.getEmail());
        assertEquals(testMember.getPhoneNumber(), result.getPhoneNumber());
    }

    @Test
    void whenMapCreateRequestToEntity_thenAllFieldsMapped() {
        // Act
        Member result = memberMapper.toEntity(createRequest);

        // Assert
        assertNotNull(result);
        assertNull(result.getId()); // ID should not be set from create request
        assertEquals(createRequest.getName(), result.getName());
        assertEquals(createRequest.getEmail(), result.getEmail());
        assertEquals(createRequest.getPhoneNumber(), result.getPhoneNumber());
    }

    @Test
    void whenUpdateEntityFromDto_thenOnlyUpdatableFieldsChanged() {
        // Act
        memberMapper.updateEntityFromDto(updateRequest, testMember);

        // Assert
        assertEquals("1", testMember.getId()); // ID should not change
        assertEquals(updateRequest.getName(), testMember.getName());
        assertEquals("test@example.com", testMember.getEmail()); // Email should not change
        assertEquals(updateRequest.getPhoneNumber(), testMember.getPhoneNumber());
    }

    @Test
    void whenMapNullEntityToDto_thenReturnNull() {
        // Act
        MemberDto result = memberMapper.toDto(null);

        // Assert
        assertNull(result);
    }

    @Test
    void whenMapNullCreateRequestToEntity_thenReturnNull() {
        // Act
        Member result = memberMapper.toEntity(null);

        // Assert
        assertNull(result);
    }

    @Test
    void whenUpdateEntityWithNullDto_thenNoChanges() {
        // Arrange
        Member originalMember = new Member();
        originalMember.setId(testMember.getId());
        originalMember.setName(testMember.getName());
        originalMember.setEmail(testMember.getEmail());
        originalMember.setPhoneNumber(testMember.getPhoneNumber());

        // Act
        memberMapper.updateEntityFromDto(null, testMember);

        // Assert
        assertEquals(originalMember.getId(), testMember.getId());
        assertEquals(originalMember.getName(), testMember.getName());
        assertEquals(originalMember.getEmail(), testMember.getEmail());
        assertEquals(originalMember.getPhoneNumber(), testMember.getPhoneNumber());
    }

    @Test
    void whenUpdateEntityWithNullFields_thenKeepExistingValues() {
        // Arrange
        UpdateMemberRequest requestWithNulls = new UpdateMemberRequest();
        requestWithNulls.setName(null);
        requestWithNulls.setPhoneNumber(null);

        String originalName = testMember.getName();
        String originalPhone = testMember.getPhoneNumber();

        // Act
        memberMapper.updateEntityFromDto(requestWithNulls, testMember);

        // Assert
        assertEquals(originalName, testMember.getName());
        assertEquals(originalPhone, testMember.getPhoneNumber());
    }

    @Test
    void whenMapDtoWithTrimmedValues_thenFieldsAreTrimmed() {
        // Arrange
        CreateMemberRequest requestWithSpaces = new CreateMemberRequest();
        requestWithSpaces.setName("  John Doe  ");
        requestWithSpaces.setEmail(" john@example.com ");
        requestWithSpaces.setPhoneNumber(" +1-555-0126 ");

        // Act
        Member result = memberMapper.toEntity(requestWithSpaces);

        // Assert
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("+1-555-0126", result.getPhoneNumber());
    }
} 