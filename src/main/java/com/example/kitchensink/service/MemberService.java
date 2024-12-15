package com.example.kitchensink.service;

import com.example.kitchensink.model.Member;
import com.example.kitchensink.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> getMemberById(String id) {
        return memberRepository.findById(id);
    }

    public Member createMember(Member member) {
        return memberRepository.save(member);
    }

    public Optional<Member> updateMember(String id, Member memberDetails) {
        return memberRepository.findById(id)
                .map(member -> {
                    member.setName(memberDetails.getName());
                    member.setEmail(memberDetails.getEmail());
                    member.setPhoneNumber(memberDetails.getPhoneNumber());
                    return memberRepository.save(member);
                });
    }

    public boolean deleteMember(String id) {
        return memberRepository.findById(id)
                .map(member -> {
                    memberRepository.delete(member);
                    return true;
                })
                .orElse(false);
    }
}

