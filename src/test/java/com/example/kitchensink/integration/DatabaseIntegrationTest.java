package com.example.kitchensink.integration;

import com.example.kitchensink.model.Member;
import com.example.kitchensink.model.User;
import com.example.kitchensink.model.Role;
import com.example.kitchensink.repository.MemberRepository;
import com.example.kitchensink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testMemberCrudOperations() {
        // Create
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("john@example.com");
        member.setPhoneNumber("1234567890");

        Member savedMember = memberRepository.save(member);
        assertNotNull(savedMember.getId());

        // Read
        Optional<Member> foundMember = memberRepository.findById(savedMember.getId());
        assertTrue(foundMember.isPresent());
        assertEquals("John Doe", foundMember.get().getName());

        // Update
        foundMember.get().setName("John Updated");
        Member updatedMember = memberRepository.save(foundMember.get());
        assertEquals("John Updated", updatedMember.getName());

        // Delete
        memberRepository.deleteById(savedMember.getId());
        assertFalse(memberRepository.findById(savedMember.getId()).isPresent());
    }

    @Test
    void testUserRegistrationAndAuthentication() {
        // Create user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRoles(new HashSet<>(Arrays.asList(Role.ROLE_USER)));

        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());

        // Find by username
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        assertTrue(foundUser.isPresent());
        assertTrue(passwordEncoder.matches("password123", foundUser.get().getPassword()));

        // Find by email
        Optional<User> foundByEmail = userRepository.findByEmail("test@example.com");
        assertTrue(foundByEmail.isPresent());
    }

    @Test
    void testUniqueConstraints() {
        // Create first member
        Member member1 = new Member();
        member1.setName("John Doe");
        member1.setEmail("john@example.com");
        member1.setPhoneNumber("1234567890");
        memberRepository.save(member1);

        // Try to create another member with same email
        Member member2 = new Member();
        member2.setName("Jane Doe");
        member2.setEmail("john@example.com");
        member2.setPhoneNumber("0987654321");

        assertThrows(DuplicateKeyException.class, () -> {
            memberRepository.save(member2);
        });
    }

    @Test
    void testPagination() {
        // Create test data
        for (int i = 0; i < 15; i++) {
            Member member = new Member();
            member.setName("User " + i);
            member.setEmail("user" + i + "@example.com");
            member.setPhoneNumber("123456789" + i);
            memberRepository.save(member);
        }

        // Test first page
        Page<Member> firstPage = memberRepository.findAll(PageRequest.of(0, 10));
        assertEquals(10, firstPage.getContent().size());
        assertEquals(15, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());

        // Test second page
        Page<Member> secondPage = memberRepository.findAll(PageRequest.of(1, 10));
        assertEquals(5, secondPage.getContent().size());
    }

    @Test
    void testSorting() {
        // Create test data
        List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
        for (String name : names) {
            Member member = new Member();
            member.setName(name);
            member.setEmail(name.toLowerCase() + "@example.com");
            member.setPhoneNumber("1234567890");
            memberRepository.save(member);
        }

        // Test sorting by name
        List<Member> sortedMembers = memberRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        assertEquals("Alice", sortedMembers.get(0).getName());
        assertEquals("Bob", sortedMembers.get(1).getName());
        assertEquals("Charlie", sortedMembers.get(2).getName());
    }

    @Test
    void testIndexes() {
        // Test email index
        assertTrue(userRepository.existsByEmail("nonexistent@example.com"));
        
        // Test username index
        assertTrue(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testCascadeOperations() {
        // Create a user with roles
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRoles(new HashSet<>(Arrays.asList(Role.ROLE_USER, Role.ROLE_ADMIN)));

        User savedUser = userRepository.save(user);

        // Verify roles are saved
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(2, foundUser.get().getRoles().size());
    }
} 