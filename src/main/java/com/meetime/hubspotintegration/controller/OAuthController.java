package com.meetime.hubspotintegration.controller;

import com.meetime.hubspotintegration.service.HubSpotOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class OAuthController {

    private final HubSpotOAuthService hubSpotOAuthService;

    public OAuthController(HubSpotOAuthService hubSpotOAuthService) {
        this.hubSpotOAuthService = hubSpotOAuthService;
    }

    @GetMapping("/url")
    public String getAuthorizationUrl(HttpServletRequest request) {
        String state = UUID.randomUUID().toString();
        request.getSession().setAttribute("OAUTH2_STATE", state);

        return hubSpotOAuthService.buildAuthorizationUrlWithState(state);
    }

    @GetMapping("/callback")
    public String handleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String returnedState,
            HttpServletRequest request) {
        String original = (String) request.getSession().getAttribute("OAUTH2_STATE");

        if (original == null || !original.equals(returnedState)) {
            throw new IllegalStateException("Invalid OAuth state");
        }

        return hubSpotOAuthService.exchangeCodeForToken(code);
    }
}