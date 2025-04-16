package com.meetime.hubspotintegration.service;

import com.meetime.hubspotintegration.exception.UnauthorizedException;
import com.meetime.hubspotintegration.util.HubSpotSecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    private final HubSpotSecurityUtils hubSpotSecurityUtils;

    public WebhookService(HubSpotSecurityUtils hubSpotSecurityUtils) {
        this.hubSpotSecurityUtils = hubSpotSecurityUtils;
    }

    public void processWebhook(String payload, String signature) {
        if (!hubSpotSecurityUtils.isValidSignature(payload, signature)) {
            throw new UnauthorizedException("Invalid webhook signature");
        }

        System.out.println("Webhook recebido e validado: " + payload);
    }
}