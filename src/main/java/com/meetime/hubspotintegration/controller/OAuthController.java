package com.meetime.hubspotintegration.controller;

import com.meetime.hubspotintegration.service.OAuthService;
import org.springframework.web.bind.annotation.*;

@RestController
public class OAuthController {

    private final OAuthService oAuthService;

    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @GetMapping("/auth/url")
    public String getAuthorizationUrl() {
        return oAuthService.generateAuthorizationUrl();
    }

    @GetMapping("/auth/callback")
    public String handleCallback(@RequestParam("code") String code) {
        return oAuthService.exchangeCodeForToken(code);
    }
}