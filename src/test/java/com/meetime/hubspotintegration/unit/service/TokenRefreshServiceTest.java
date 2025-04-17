package com.meetime.hubspotintegration.unit.service;

import com.meetime.hubspotintegration.service.HubSpotOAuthService;
import com.meetime.hubspotintegration.service.TokenRefreshService;
import com.meetime.hubspotintegration.token.TokenStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

class TokenRefreshServiceTest {

    private TokenStore tokenStore;
    private HubSpotOAuthService oauth;
    private TokenRefreshService service;

    @BeforeEach
    void setup() {
        tokenStore = mock(TokenStore.class);
        oauth = mock(HubSpotOAuthService.class);
        service = new TokenRefreshService(oauth, tokenStore);
    }

    @Test
    void shouldRefreshTokenWhenRefreshTokenIsPresent() {
        // Arrange
        when(tokenStore.getRefreshToken()).thenReturn(Optional.of("refresh123"));
        when(oauth.refreshAccessToken("refresh123")).thenReturn("newAccess");

        // Act
        service.refreshToken();

        // Assert
        verify(oauth).refreshAccessToken("refresh123");
        verify(tokenStore).storeAccessToken("newAccess");
    }

    @Test
    void shouldSkipWhenRefreshTokenIsMissing() {
        // Arrange
        when(tokenStore.getRefreshToken()).thenReturn(Optional.empty());

        // Act
        service.refreshToken();

        // Assert
        verify(oauth, never()).refreshAccessToken(any());
        verify(tokenStore, never()).storeAccessToken(any());
    }
}
