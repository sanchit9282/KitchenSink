package com.example.kitchensink.controller;

import com.example.kitchensink.dto.AuthResponse;
import com.example.kitchensink.dto.LoginRequest;
import com.example.kitchensink.dto.RegisterRequest;
import com.example.kitchensink.dto.TokenRefreshRequest;
import com.example.kitchensink.dto.TokenRefreshResponse;
import com.example.kitchensink.model.Role;
import com.example.kitchensink.model.User;
import com.example.kitchensink.model.RefreshToken;
import com.example.kitchensink.repository.UserRepository;
import com.example.kitchensink.security.JwtUtils;
import com.example.kitchensink.security.UserDetailsImpl;
import com.example.kitchensink.service.RefreshTokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.debug("Login attempt for user: {}", loginRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // Create refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
            
            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toSet());

            logger.info("User logged in successfully: {}", loginRequest.getUsername());
            logger.debug("User roles: {}", roles);

            return ResponseEntity.ok(new AuthResponse(jwt, refreshToken.getToken(), userDetails.getUsername(), roles));
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        User user = new User(
            registerRequest.getUsername(),
            registerRequest.getEmail(),
            encoder.encode(registerRequest.getPassword())
        );

        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        roles.add(Role.ROLE_ADMIN);
        user.setRoles(roles);

        userRepository.save(user);
        logger.info("User registered successfully with roles: {}", roles);

        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
            .map(refreshTokenService::verifyExpiration)
            .map(RefreshToken::getUserId)
            .map(userId -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
                String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
            })
            .orElseThrow(() -> new RuntimeException("Refresh token not found"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@Valid @RequestBody TokenRefreshRequest request) {
        try {
            String userId = refreshTokenService.findByToken(request.getRefreshToken())
                .map(RefreshToken::getUserId)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
            
            refreshTokenService.deleteByUserId(userId);
            return ResponseEntity.ok("Log out successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
} 