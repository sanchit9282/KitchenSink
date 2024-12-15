package com.example.kitchensink.security;

import com.example.kitchensink.model.User;
import com.example.kitchensink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // Try to find by email first
        Optional<User> userByEmail = userRepository.findByEmail(login);
        if (userByEmail.isPresent()) {
            return UserDetailsImpl.build(userByEmail.get());
        }

        // If not found by email, try username
        User user = userRepository.findByUsername(login)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + login));

        return UserDetailsImpl.build(user);
    }
} 