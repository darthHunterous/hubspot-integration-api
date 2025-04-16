package com.meetime.hubspotintegration.service;

import com.meetime.hubspotintegration.util.HubSpotSecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    private final HubSpotSecurityUtils hubSpotSecurityUtils;

    public WebhookService(HubSpotSecurityUtils hubSpotSecurityUtils) {
        this.hubSpotSecurityUtils = hubSpotSecurityUtils;
    }

    public void processWebhook(String payload, String signature, String timestamp,
                               String method, String uri) {
        String baseString = method + uri + payload + timestamp;

        if (!hubSpotSecurityUtils.isValidSignature(payload, signature)) {
            throw new RuntimeException("Invalid webhook signature");
        }
        // Processar payload conforme necessário
        System.out.println("Webhook recebido e validado: " + payload);
    }
}