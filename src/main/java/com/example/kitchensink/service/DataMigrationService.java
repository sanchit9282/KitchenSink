package com.example.kitchensink.service;

import com.example.kitchensink.model.Role;
import com.example.kitchensink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DataMigrationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @EventListener(ApplicationReadyEvent.class)
    public void migrateUserRoles() {
        userRepository.findAll().forEach(user -> {
            if (!user.getRoles().contains(Role.ROLE_ADMIN)) {
                user.getRoles().add(Role.ROLE_ADMIN);
                userRepository.save(user);
            }
        });
    }
} 