package com.example.kitchensink.controller;

import com.example.kitchensink.dto.CreateMemberRequest;
import com.example.kitchensink.dto.MemberDto;
import com.example.kitchensink.dto.UpdateMemberRequest;
import com.example.kitchensink.dto.response.ApiResponse;
import com.example.kitchensink.dto.response.PagedResponse;
import com.example.kitchensink.mapper.MemberMapper;
import com.example.kitchensink.model.Member;
import com.example.kitchensink.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberControllerTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberController memberController;

    private Member testMember;
    private MemberDto testMemberDto;
    private CreateMemberRequest createRequest;
    private UpdateMemberRequest updateRequest;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setId("1");
        testMember.setName("Test User");
        testMember.setEmail("test@example.com");
        testMember.setPhoneNumber("1234567890");

        testMemberDto = new MemberDto();
        testMemberDto.setId("1");
        testMemberDto.setName("Test User");
        testMemberDto.setEmail("test@example.com");
        testMemberDto.setPhoneNumber("1234567890");

        createRequest = new CreateMemberRequest();
        createRequest.setName("Test User");
        createRequest.setEmail("test@example.com");
        createRequest.setPhoneNumber("1234567890");

        updateRequest = new UpdateMemberRequest();
        updateRequest.setName("Test User Updated");
        updateRequest.setEmail("test.updated@example.com");
        updateRequest.setPhoneNumber("0987654321");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllMembers_Success() {
        // Arrange
        List<Member> members = Arrays.asList(testMember);
        Page<Member> memberPage = new PageImpl<>(members);
        when(memberRepository.findAll(any(Pageable.class))).thenReturn(memberPage);
        when(memberMapper.toDto(any(Member.class))).thenReturn(testMemberDto);

        // Act
        ResponseEntity<?> response = memberController.getAllMembers(0, 10, "name", "asc");

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        @SuppressWarnings("unchecked")
        ApiResponse<PagedResponse<MemberDto>> apiResponse = (ApiResponse<PagedResponse<MemberDto>>) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertNotNull(apiResponse.getData());
        assertEquals(1, apiResponse.getData().getContent().size());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMember_Success() {
        // Arrange
        when(memberMapper.toEntity(any(CreateMemberRequest.class))).thenReturn(testMember);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(memberMapper.toDto(any(Member.class))).thenReturn(testMemberDto);

        // Act
        ResponseEntity<?> response = memberController.createMember(createRequest);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        @SuppressWarnings("unchecked")
        ApiResponse<MemberDto> apiResponse = (ApiResponse<MemberDto>) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertNotNull(apiResponse.getData());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMember_Success() {
        // Arrange
        when(memberRepository.findById("1")).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(memberMapper.toDto(any(Member.class))).thenReturn(testMemberDto);

        // Act
        ResponseEntity<?> response = memberController.updateMember("1", updateRequest);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMember_Success() {
        // Arrange
        when(memberRepository.findById("1")).thenReturn(Optional.of(testMember));
        doNothing().when(memberRepository).delete(any(Member.class));

        // Act
        ResponseEntity<?> response = memberController.deleteMember("1");

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(memberRepository).delete(any(Member.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllMembers_InvalidSortField() {
        // Act
        ResponseEntity<?> response = memberController.getAllMembers(0, 10, "invalid_field", "asc");

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        @SuppressWarnings("unchecked")
        ApiResponse<PagedResponse<MemberDto>> apiResponse = (ApiResponse<PagedResponse<MemberDto>>) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertNotNull(apiResponse.getMessage());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMember_NotFound() {
        // Arrange
        when(memberRepository.findById("999")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = memberController.updateMember("999", updateRequest);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        @SuppressWarnings("unchecked")
        ApiResponse<MemberDto> apiResponse = (ApiResponse<MemberDto>) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("not found"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createMember_WithUserRole_ShouldFail() {
        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            memberController.createMember(createRequest);
        });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMember_WithDuplicateEmail_ShouldFail() {
        // Arrange
        when(memberMapper.toEntity(any(CreateMemberRequest.class))).thenReturn(testMember);
        when(memberRepository.save(any(Member.class)))
            .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        // Act
        ResponseEntity<?> response = memberController.createMember(createRequest);

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        @SuppressWarnings("unchecked")
        ApiResponse<MemberDto> apiResponse = (ApiResponse<MemberDto>) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
    }

    @Test
    void getAllMembers_WithoutAuthentication_ShouldFail() {
        // Act & Assert
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            memberController.getAllMembers(0, 10, "name", "asc");
        });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllMembers_WithPagination_ShouldReturnCorrectPage() {
        // Arrange
        List<Member> members = Arrays.asList(testMember);
        Page<Member> memberPage = new PageImpl<>(members, PageRequest.of(0, 10), 1);
        when(memberRepository.findAll(any(Pageable.class))).thenReturn(memberPage);
        when(memberMapper.toDto(any(Member.class))).thenReturn(testMemberDto);

        // Act
        ResponseEntity<?> response = memberController.getAllMembers(0, 10, "name", "asc");

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        @SuppressWarnings("unchecked")
        ApiResponse<PagedResponse<MemberDto>> apiResponse = (ApiResponse<PagedResponse<MemberDto>>) response.getBody();
        assertNotNull(apiResponse);
        assertNotNull(apiResponse.getData());
        PagedResponse<MemberDto> pagedResponse = apiResponse.getData();
        assertEquals(1, pagedResponse.getTotalElements());
        assertEquals(1, pagedResponse.getTotalPages());
    }
} 