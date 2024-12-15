package com.example.kitchensink.repository;

import com.example.kitchensink.model.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemberRepository extends MongoRepository<Member, String> {
    boolean existsByEmail(String email);
}

