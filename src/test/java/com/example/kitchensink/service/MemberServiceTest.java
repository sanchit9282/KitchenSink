package com.example.kitchensink.service;

import com.example.kitchensink.model.Member;
import com.example.kitchensink.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllMembers() {
        Member member1 = new Member();
        member1.setId("1");
        member1.setName("John Doe");
        Member member2 = new Member();
        member2.setId("2");
        member2.setName("Jane Doe");

        when(memberRepository.findAll()).thenReturn(Arrays.asList(member1, member2));

        List<Member> members = memberService.getAllMembers();

        assertEquals(2, members.size());
        verify(memberRepository, times(1)).findAll();
    }

    @Test
    void getMemberById() {
        Member member = new Member();
        member.setId("1");
        member.setName("John Doe");

        when(memberRepository.findById("1")).thenReturn(Optional.of(member));

        Optional<Member> foundMember = memberService.getMemberById("1");

        assertTrue(foundMember.isPresent());
        assertEquals("John Doe", foundMember.get().getName());
        verify(memberRepository, times(1)).findById("1");
    }

    @Test
    void createMember() {
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("john@example.com");
        member.setPhoneNumber("1234567890");

        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Member createdMember = memberService.createMember(member);

        assertNotNull(createdMember);
        assertEquals("John Doe", createdMember.getName());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void updateMember() {
        Member existingMember = new Member();
        existingMember.setId("1");
        existingMember.setName("John Doe");
        existingMember.setEmail("john@example.com");
        existingMember.setPhoneNumber("1234567890");

        Member updatedMember = new Member();
        updatedMember.setName("John Updated");
        updatedMember.setEmail("johnupdated@example.com");
        updatedMember.setPhoneNumber("0987654321");

        when(memberRepository.findById("1")).thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class))).thenReturn(updatedMember);

        Optional<Member> result = memberService.updateMember("1", updatedMember);

        assertTrue(result.isPresent());
        assertEquals("John Updated", result.get().getName());
        assertEquals("johnupdated@example.com", result.get().getEmail());
        assertEquals("0987654321", result.get().getPhoneNumber());
        verify(memberRepository, times(1)).findById("1");
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void deleteMember() {
        Member member = new Member();
        member.setId("1");
        member.setName("John Doe");

        when(memberRepository.findById("1")).thenReturn(Optional.of(member));

        boolean result = memberService.deleteMember("1");

        assertTrue(result);
        verify(memberRepository, times(1)).findById("1");
        verify(memberRepository, times(1)).delete(any(Member.class));
    }
}

