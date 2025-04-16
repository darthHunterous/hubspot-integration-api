package com.meetime.hubspotintegration.service;

import com.meetime.hubspotintegration.exception.UnauthorizedException;
import com.meetime.hubspotintegration.util.HubSpotSecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private HubSpotSecurityUtils securityUtils;

    @InjectMocks
    private WebhookService webhookService;

    @Test
    void processWebhook_withValidSignature_shouldNotThrow() {
        // Arrange
        String payload   = "{\"foo\":\"bar\"}";
        String signature = "valid-signature";
        when(securityUtils.isValidSignature(payload, signature)).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> webhookService.processWebhook(payload, signature));
        verify(securityUtils).isValidSignature(payload, signature);
    }

    @Test
    void processWebhook_withInvalidSignature_shouldThrowUnauthorizedException() {
        // Arrange
        String payload   = "{\"foo\":\"bar\"}";
        String signature = "invalid-signature";
        when(securityUtils.isValidSignature(payload, signature)).thenReturn(false);

        // Act & Assert
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> webhookService.processWebhook(payload, signature)
        );
        assertEquals("Invalid webhook signature", ex.getMessage());
        verify(securityUtils).isValidSignature(payload, signature);
    }
}
