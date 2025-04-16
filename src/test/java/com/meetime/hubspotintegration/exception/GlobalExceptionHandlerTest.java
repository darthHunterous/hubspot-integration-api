package com.meetime.hubspotintegration.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test/path");
    }

    @Test
    void handleInvalidTokenException() {
        HubSpotIntegrationException ex = new HubSpotIntegrationException("fail in hubspot", null);

        ResponseEntity<ApiError> resp = handler.handleHubSpot(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ApiError body = resp.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Bad Request", body.getError());
        assertEquals("fail in hubspot", body.getMessage());
        assertEquals("/test/path", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void handleValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult br = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(br);
        FieldError fe = new FieldError("obj", "field", "must not be blank");
        when(br.getFieldErrors()).thenReturn(List.of(fe));

        ResponseEntity<ApiError> resp = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ApiError body = resp.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Bad Request", body.getError());
        assertTrue(body.getMessage().contains("field: must not be blank"));
        assertEquals("/test/path", body.getPath());
    }

    @Test
    void handleUnexpectedException() {
        Exception ex = new RuntimeException("boom");

        ResponseEntity<ApiError> resp = handler.handleUnexpected(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        ApiError body = resp.getBody();
        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertEquals("Internal Server Error", body.getError());
        assertEquals("Internal server error", body.getMessage());
        assertEquals("/test/path", body.getPath());
    }
}
