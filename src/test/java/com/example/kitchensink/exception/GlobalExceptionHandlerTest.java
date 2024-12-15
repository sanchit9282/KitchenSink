package com.example.kitchensink.exception;

import com.example.kitchensink.dto.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void whenHandleMethodArgumentNotValid_thenReturnValidationErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        
        FieldError fieldError = new FieldError("object", "field", "error message");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleMethodArgumentNotValid(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("field"));
        assertTrue(response.getBody().getMessage().contains("error message"));
    }

    @Test
    void whenHandleConstraintViolation_thenReturnValidationErrors() {
        // Arrange
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getMessage()).thenReturn("validation error");
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException("Validation failed", violations);

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleConstraintViolation(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("validation error"));
    }

    @Test
    void whenHandleAccessDenied_thenReturnForbidden() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleAccessDenied(ex);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Access denied"));
    }

    @Test
    void whenHandleAuthenticationException_thenReturnUnauthorized() {
        // Arrange
        AuthenticationException ex = new BadCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleAuthenticationException(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Invalid credentials"));
    }

    @Test
    void whenHandleResourceNotFound_thenReturnNotFound() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleResourceNotFound(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Resource not found"));
    }

    @Test
    void whenHandleDuplicateResource_thenReturnConflict() {
        // Arrange
        DuplicateResourceException ex = new DuplicateResourceException("Resource already exists");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleDuplicateResource(ex);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Resource already exists"));
    }

    @Test
    void whenHandleUnexpectedException_thenReturnInternalError() {
        // Arrange
        RuntimeException ex = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleUnexpectedException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Unexpected error"));
    }

    @Test
    void whenHandleTokenRefreshException_thenReturnForbidden() {
        // Arrange
        TokenRefreshException ex = new TokenRefreshException("Invalid refresh token");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleTokenRefreshException(ex);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Invalid refresh token"));
    }

    @Test
    void whenHandleMultipleValidationErrors_thenCombineMessages() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        
        List<FieldError> fieldErrors = List.of(
            new FieldError("object", "field1", "error1"),
            new FieldError("object", "field2", "error2")
        );
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleMethodArgumentNotValid(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        String message = response.getBody().getMessage();
        assertTrue(message.contains("field1"));
        assertTrue(message.contains("error1"));
        assertTrue(message.contains("field2"));
        assertTrue(message.contains("error2"));
    }
} 