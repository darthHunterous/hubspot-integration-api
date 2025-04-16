package com.meetime.hubspotintegration.unit.service;

import com.meetime.hubspotintegration.service.HubSpotOAuthService;
import com.meetime.hubspotintegration.service.TokenRefreshService;
import com.meetime.hubspotintegration.token.TokenStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TokenRefreshServiceTest {

    @Mock
    private HubSpotOAuthService oauthService;

    @Mock
    private TokenStore tokenStore;

    @InjectMocks
    private TokenRefreshService refreshService;

    @Test
    void refreshToken_withExistingRefreshToken_callsOauthRefresh() {
        // Arrange
        when(tokenStore.getRefreshToken()).thenReturn(Optional.of("existing-refresh"));

        // Act
        refreshService.refreshToken();

        // Assert
        verify(tokenStore).getRefreshToken();
        verify(oauthService).refreshAccessToken("existing-refresh");
    }

    @Test
    void refreshToken_withoutRefreshToken_throwsIllegalStateException() {
        // Arrange
        when(tokenStore.getRefreshToken()).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> refreshService.refreshToken()
        );
        assertEquals("No refresh token", ex.getMessage());

        verify(tokenStore).getRefreshToken();
        verifyNoInteractions(oauthService);
    }
}
