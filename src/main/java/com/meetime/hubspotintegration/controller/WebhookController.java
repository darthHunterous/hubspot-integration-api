package com.meetime.hubspotintegration.controller;

import com.meetime.hubspotintegration.service.WebhookService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/contact")
    public void handleContactWebhook(@RequestBody String payload,
                                     @RequestHeader("X-HubSpot-Signature") String signature,
                                     @RequestHeader("X-HubSpot-Request-Timestamp") String timestamp) {
        webhookService.processWebhook(payload, signature, timestamp);
    }
}
