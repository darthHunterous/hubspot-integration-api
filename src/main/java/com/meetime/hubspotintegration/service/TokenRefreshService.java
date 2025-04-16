package com.meetime.hubspotintegration.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.meetime.hubspotintegration.token.TokenStore;

@Service
public class TokenRefreshService {

    private final HubSpotOAuthService oauth;
    private final TokenStore store;

    public TokenRefreshService(HubSpotOAuthService oauth, TokenStore store) {
        this.oauth = oauth;
        this.store = store;
    }

    @Scheduled(fixedRateString = "${hubspot.refresh.interval.ms:1800000}")
    public void refreshToken() {
        String currentRefresh = store.getRefreshToken()
                .orElseThrow(() -> new IllegalStateException("No refresh token"));
        oauth.refreshAccessToken(currentRefresh);
    }
}
