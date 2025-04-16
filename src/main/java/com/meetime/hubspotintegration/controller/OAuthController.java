package com.meetime.hubspotintegration.controller;

import com.meetime.hubspotintegration.service.HubSpotOAuthService;
import org.springframework.web.bind.annotation.*;

@RestController
public class OAuthController {

    private final HubSpotOAuthService hubSpotOAuthService;

    public OAuthController(HubSpotOAuthService hubSpotOAuthService) {
        this.hubSpotOAuthService = hubSpotOAuthService;
    }

    @GetMapping("/auth/url")
    public String getAuthorizationUrl() {
        return hubSpotOAuthService.buildAuthorizationUrl();
    }

    @GetMapping("/auth/callback")
    public String handleCallback(@RequestParam("code") String code) {
        return hubSpotOAuthService.exchangeCodeForToken(code);
    }
}