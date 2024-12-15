package com.example.kitchensink.controller;

import com.example.kitchensink.dto.CreateMemberRequest;
import com.example.kitchensink.dto.MemberDto;
import com.example.kitchensink.dto.UpdateMemberRequest;
import com.example.kitchensink.dto.response.ApiResponse;
import com.example.kitchensink.dto.response.PagedResponse;
import com.example.kitchensink.mapper.MemberMapper;
import com.example.kitchensink.model.Member;
import com.example.kitchensink.repository.MemberRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/members")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class MemberController {
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberMapper memberMapper;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PagedResponse<MemberDto>>> getAllMembers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name") @Pattern(regexp = "^(name|email|phoneNumber)$", 
                message = "Sort by must be one of: name, email, phoneNumber") String sortBy,
            @RequestParam(defaultValue = "asc") @Pattern(regexp = "^(asc|desc)$", 
                message = "Direction must be either 'asc' or 'desc'") String direction) {
        
        try {
            logger.debug("Fetching members page {} of size {}, sorted by {} {}", page, size, sortBy, direction);
            
            // Validate sortBy field exists in Member class
            if (!isSortByFieldValid(sortBy)) {
                return ResponseEntity.ok(ApiResponse.error("Invalid sort field: " + sortBy));
            }
            
            Sort.Direction sortDirection;
            try {
                sortDirection = Sort.Direction.fromString(direction.toLowerCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.ok(ApiResponse.error("Invalid sort direction: " + direction));
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            
            Page<Member> memberPage = memberRepository.findAll(pageable);
            
            PagedResponse<MemberDto> response = new PagedResponse<>();
            response.setContent(memberPage.getContent().stream()
                    .map(memberMapper::toDto)
                    .collect(Collectors.toList()));
            response.setPage(memberPage.getNumber());
            response.setSize(memberPage.getSize());
            response.setTotalElements(memberPage.getTotalElements());
            response.setTotalPages(memberPage.getTotalPages());
            response.setLast(memberPage.isLast());

            logger.debug("Returning {} members", response.getContent().size());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            logger.error("Error fetching members: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Error fetching members: " + e.getMessage()));
        }
    }

    private boolean isSortByFieldValid(String sortBy) {
        try {
            Member.class.getDeclaredField(sortBy);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MemberDto>> createMember(@Valid @RequestBody CreateMemberRequest request) {
        logger.debug("Creating new member with email: {}", request.getEmail());
        try {
            Member member = memberMapper.toEntity(request);
            Member savedMember = memberRepository.save(member);
            logger.info("Successfully created member with id: {}", savedMember.getId());
            return ResponseEntity.ok(ApiResponse.success("Member created successfully", memberMapper.toDto(savedMember)));
        } catch (Exception e) {
            logger.error("Error creating member: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create member"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MemberDto>> updateMember(
            @PathVariable String id,
            @Valid @RequestBody UpdateMemberRequest request) {
        logger.debug("Updating member with id: {}", id);
        return memberRepository.findById(id)
                .map(member -> {
                    memberMapper.updateEntityFromDto(request, member);
                    Member updatedMember = memberRepository.save(member);
                    logger.info("Successfully updated member with id: {}", id);
                    return ResponseEntity.ok(ApiResponse.success("Member updated successfully", 
                        memberMapper.toDto(updatedMember)));
                })
                .orElseGet(() -> {
                    logger.warn("Member not found with id: {}", id);
                    return ResponseEntity.ok(ApiResponse.error("Member not found with id: " + id));
                });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deleteMember(@PathVariable String id) {
        logger.debug("Deleting member with id: {}", id);
        return memberRepository.findById(id)
                .map(member -> {
                    memberRepository.delete(member);
                    logger.info("Successfully deleted member with id: {}", id);
                    return ResponseEntity.ok(ApiResponse.success("Member deleted successfully", null));
                })
                .orElseGet(() -> {
                    logger.warn("Member not found with id: {}", id);
                    return ResponseEntity.ok(ApiResponse.error("Member not found with id: " + id));
                });
    }
}

