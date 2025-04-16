package com.meetime.hubspotintegration.unit.service;

import com.meetime.hubspotintegration.exception.UnauthorizedException;
import com.meetime.hubspotintegration.service.WebhookService;
import com.meetime.hubspotintegration.util.HubSpotSecurityUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.Instant;

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
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        when(securityUtils.isValidSignature(payload, signature)).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> webhookService.processWebhook(payload, signature, timestamp));
        verify(securityUtils).isValidSignature(payload, signature);
    }

    @Test
    void processWebhook_withInvalidSignature_shouldThrowUnauthorizedException() {
        // Arrange
        String payload   = "{\"foo\":\"bar\"}";
        String signature = "invalid-signature";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        when(securityUtils.isValidSignature(payload, signature)).thenReturn(false);

        // Act & Assert
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> webhookService.processWebhook(payload, signature, timestamp)
        );
        assertEquals("Invalid webhook signature", ex.getMessage());
        verify(securityUtils).isValidSignature(payload, signature);
    }

    @Test
    void processWebhook_withOldTimestamp_shouldThrowUnauthorized() {
        String payload = "{\"foo\":\"bar\"}";
        String signature = "dummy";

        long oldTimestamp = Instant.now().minusSeconds(600).getEpochSecond();

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> webhookService.processWebhook(payload, signature, String.valueOf(oldTimestamp))
        );

        assertEquals("Timestamp expired or too far from server time", ex.getMessage());
    }
}
