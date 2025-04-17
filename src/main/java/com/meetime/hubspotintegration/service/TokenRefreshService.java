package com.meetime.hubspotintegration.service;

import com.meetime.hubspotintegration.token.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TokenRefreshService {

    private static final Logger log = LoggerFactory.getLogger(TokenRefreshService.class);

    private final HubSpotOAuthService oauth;
    private final TokenStore store;

    public TokenRefreshService(HubSpotOAuthService oauth, TokenStore store) {
        this.oauth = oauth;
        this.store = store;
    }

    @Scheduled(fixedRateString = "${hubspot.refresh.interval.ms:1800000}")
    public void refreshToken() {
        store.getRefreshToken().ifPresentOrElse(
                refresh -> {
                    try {
                        String newAccess = oauth.refreshAccessToken(refresh);
                        store.storeAccessToken(newAccess);
                        log.info("Access token successfully refreshed.");
                    } catch (Exception e) {
                        log.error("Error while refreshing access token: {}", e.getMessage(), e);
                    }
                },
                () -> log.warn("Refresh token not available — skipping scheduled refresh.")
        );
    }
}
