package com.meetime.hubspotintegration.service;

import com.meetime.hubspotintegration.exception.UnauthorizedException;
import com.meetime.hubspotintegration.util.HubSpotSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final HubSpotSecurityUtils hubSpotSecurityUtils;

    public WebhookService(HubSpotSecurityUtils hubSpotSecurityUtils) {
        this.hubSpotSecurityUtils = hubSpotSecurityUtils;
    }

    public void processWebhook(String payload, String signature, String timestampHeader) {
        long requestTimeInMilliseconds;
        try {
            requestTimeInMilliseconds = Long.parseLong(timestampHeader);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid timestamp format");
        }

        long now = Instant.now().toEpochMilli();
        long difference = Math.abs(now - requestTimeInMilliseconds);

        if (difference > Duration.ofMinutes(5).toMillis()) {
            throw new UnauthorizedException("Timestamp expired or too far from server time");
        }

        if (!hubSpotSecurityUtils.isValidSignature(payload, signature)) {
            throw new UnauthorizedException("Invalid webhook signature");
        }

        log.info("Webhook signature validated, payload received: {}", payload);
    }
}