package com.meetime.hubspotintegration.unit.service;

import com.meetime.hubspotintegration.exception.UnauthorizedException;
import com.meetime.hubspotintegration.service.WebhookService;
import com.meetime.hubspotintegration.util.HubSpotSecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private HubSpotSecurityUtils securityUtils;

    private WebhookService webhookService;

    @BeforeEach
    void setUp() {
        webhookService = new WebhookService(securityUtils);
    }

    @Test
    void shouldAcceptValidTimestampAndSignature() {
        String payload = "{\"foo\":\"bar\"}";
        String signature = "valid-signature";
        long nowMillis = Instant.now().toEpochMilli();

        when(securityUtils.isValidSignature(payload, signature)).thenReturn(true);

        assertDoesNotThrow(() -> webhookService.processWebhook(
                payload, signature, String.valueOf(nowMillis))
        );

        verify(securityUtils).isValidSignature(payload, signature);
    }

    @Test
    void shouldRejectIfTimestampTooOld() {
        String payload = "{}";
        String signature = "valid";
        long oldMillis = Instant.now().minus(Duration.ofMinutes(6)).toEpochMilli();

        assertThrows(UnauthorizedException.class, () ->
                webhookService.processWebhook(payload, signature, String.valueOf(oldMillis))
        );

        verify(securityUtils, never()).isValidSignature(any(), any());
    }

    @Test
    void shouldRejectIfTimestampTooFarInFuture() {
        String payload = "{}";
        String signature = "valid";
        long futureMillis = Instant.now().plus(Duration.ofMinutes(10)).toEpochMilli();

        assertThrows(UnauthorizedException.class, () ->
                webhookService.processWebhook(payload, signature, String.valueOf(futureMillis))
        );

        verify(securityUtils, never()).isValidSignature(any(), any());
    }

    @Test
    void shouldRejectIfTimestampIsInvalidFormat() {
        String payload = "{}";
        String signature = "valid";
        String badTimestamp = "abc";

        assertThrows(UnauthorizedException.class, () ->
                webhookService.processWebhook(payload, signature, badTimestamp)
        );

        verify(securityUtils, never()).isValidSignature(any(), any());
    }

    @Test
    void shouldRejectIfSignatureIsInvalid() {
        String payload = "{\"foo\":\"bar\"}";
        String signature = "invalid";
        long nowMillis = Instant.now().toEpochMilli();

        when(securityUtils.isValidSignature(payload, signature)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () ->
                webhookService.processWebhook(payload, signature, String.valueOf(nowMillis))
        );
    }
}
