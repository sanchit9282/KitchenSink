package com.example.kitchensink.controller;

import com.example.kitchensink.model.Member;
import com.example.kitchensink.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void testCreateMember() throws Exception {
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("john@example.com");
        member.setPhoneNumber("1234567890");

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("1234567890"));
    }

    @Test
    void testGetAllMembers() throws Exception {
        Member member1 = new Member();
        member1.setName("John Doe");
        member1.setEmail("john@example.com");
        member1.setPhoneNumber("1234567890");
        memberRepository.save(member1);

        Member member2 = new Member();
        member2.setName("Jane Doe");
        member2.setEmail("jane@example.com");
        member2.setPhoneNumber("0987654321");
        memberRepository.save(member2);

        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Doe"));
    }

    @Test
    void testUpdateMember() throws Exception {
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("john@example.com");
        member.setPhoneNumber("1234567890");
        Member savedMember = memberRepository.save(member);

        Member updatedMember = new Member();
        updatedMember.setName("John Updated");
        updatedMember.setEmail("johnupdated@example.com");
        updatedMember.setPhoneNumber("0987654321");

        mockMvc.perform(put("/api/members/" + savedMember.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("johnupdated@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("0987654321"));
    }

    @Test
    void testDeleteMember() throws Exception {
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("john@example.com");
        member.setPhoneNumber("1234567890");
        Member savedMember = memberRepository.save(member);

        mockMvc.perform(delete("/api/members/" + savedMember.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/members/" + savedMember.getId()))
                .andExpect(status().isNotFound());
    }
}

