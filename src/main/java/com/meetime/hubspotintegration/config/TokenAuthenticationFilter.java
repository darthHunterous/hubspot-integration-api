package com.meetime.hubspotintegration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspotintegration.exception.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final String expectedToken;
    private final ObjectMapper objectMapper;

    public TokenAuthenticationFilter(String expectedToken, ObjectMapper objectMapper) {
        this.expectedToken = expectedToken;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if ("/contacts".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.equals("Bearer " + expectedToken)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");

                ApiError error = new ApiError(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized",
                        "Authentication token absent or invalid",
                        request.getRequestURI()
                );

                objectMapper.writeValue(response.getWriter(), error);
                return;
            }

            SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            "contact-api-user", null, java.util.Collections.emptyList()
                    )
            );
        }

        filterChain.doFilter(request, response);
    }
}
