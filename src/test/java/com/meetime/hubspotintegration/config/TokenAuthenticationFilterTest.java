package com.meetime.hubspotintegration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspotintegration.exception.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TokenAuthenticationFilterTest {

    private static final String VALID_TOKEN = "meu-token-secreto";

    private TokenAuthenticationFilter filter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        filter = new TokenAuthenticationFilter(VALID_TOKEN, objectMapper);
    }

    @Test
    void shouldAllowRequestWithValidToken() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/contacts");
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
        assertEquals(200, response.getStatus());
        Mockito.verify(chain).doFilter(request, response);
    }

    @Test
    void shouldReturn401IfTokenIsMissing() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/contacts");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
        assertEquals(401, response.getStatus());

        ApiError error = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertEquals("Authentication token absent or invalid", error.getMessage());
        assertEquals("/contacts", error.getPath());
    }

    @Test
    void shouldReturn401IfTokenIsInvalid() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/contacts");
        request.addHeader("Authorization", "Bearer invalid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
        assertEquals(401, response.getStatus());

        ApiError error = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertEquals("Authentication token absent or invalid", error.getMessage());
    }

    @Test
    void shouldNotFilterOtherEndpoints() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/url");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
        assertEquals(200, response.getStatus());
        Mockito.verify(chain).doFilter(request, response);
    }
}
