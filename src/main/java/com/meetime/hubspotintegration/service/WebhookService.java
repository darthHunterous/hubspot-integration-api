package com.meetime.hubspotintegration.service;

import com.meetime.hubspotintegration.exception.UnauthorizedException;
import com.meetime.hubspotintegration.util.HubSpotSecurityUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class WebhookService {

    private static final long MAX_TIME_APART_IN_SECONDS = 5 * 60;

    private final HubSpotSecurityUtils hubSpotSecurityUtils;

    public WebhookService(HubSpotSecurityUtils hubSpotSecurityUtils) {
        this.hubSpotSecurityUtils = hubSpotSecurityUtils;
    }

    public void processWebhook(String payload, String signature, String timestampHeader) {
        long requestEpochSeconds;
        try {
            requestEpochSeconds = Long.parseLong(timestampHeader);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid timestamp format");
        }

        long now = Instant.now().getEpochSecond();
        long drift = Math.abs(now - requestEpochSeconds);
        if (drift > MAX_TIME_APART_IN_SECONDS) {
            throw new UnauthorizedException("Timestamp expired or too far from server time");
        }

        if (!hubSpotSecurityUtils.isValidSignature(payload, signature)) {
            throw new UnauthorizedException("Invalid webhook signature");
        }

        System.out.println("Webhook signature validated, payload received: " + payload);
    }
}