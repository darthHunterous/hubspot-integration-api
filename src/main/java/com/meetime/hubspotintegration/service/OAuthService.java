package com.meetime.hubspotintegration.service;

import com.meetime.hubspotintegration.client.HubSpotClient;
import org.springframework.stereotype.Service;

@Service
public class OAuthService {

    private final HubSpotClient hubSpotClient;

    public OAuthService(HubSpotClient hubSpotClient) {
        this.hubSpotClient = hubSpotClient;
    }

    public String generateAuthorizationUrl() {
        return hubSpotClient.buildAuthorizationUrl();
    }

    public String exchangeCodeForToken(String code) {
        return hubSpotClient.exchangeToken(code);
    }
}