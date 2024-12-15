package com.example.kitchensink.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Log request
        logger.debug("REQUEST  {} : {}", request.getMethod(), request.getRequestURI());
        logger.debug("Remote Host: {}", request.getRemoteHost());
        logger.debug("Authorization: {}", request.getHeader("Authorization"));

        // Continue with the filter chain
        filterChain.doFilter(request, response);

        // Log response
        logger.debug("RESPONSE : {}", response.getStatus());
    }
} 