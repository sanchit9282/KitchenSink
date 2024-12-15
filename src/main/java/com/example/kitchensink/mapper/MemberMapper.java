package com.example.kitchensink.mapper;

import com.example.kitchensink.dto.CreateMemberRequest;
import com.example.kitchensink.dto.MemberDto;
import com.example.kitchensink.dto.UpdateMemberRequest;
import com.example.kitchensink.model.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
    
    public MemberDto toDto(Member member) {
        MemberDto dto = new MemberDto();
        dto.setId(member.getId());
        dto.setName(member.getName());
        dto.setEmail(member.getEmail());
        dto.setPhoneNumber(member.getPhoneNumber());
        return dto;
    }

    public Member toEntity(CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhoneNumber(request.getPhoneNumber());
        return member;
    }

    public void updateEntityFromDto(UpdateMemberRequest request, Member member) {
        member.setName(request.getName());
        member.setPhoneNumber(request.getPhoneNumber());
    }
} 