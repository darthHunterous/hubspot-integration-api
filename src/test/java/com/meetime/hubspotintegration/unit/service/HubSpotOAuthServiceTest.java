package com.meetime.hubspotintegration.unit.service;

import com.meetime.hubspotintegration.dto.OAuthTokenResponse;
import com.meetime.hubspotintegration.exception.HubSpotIntegrationException;
import com.meetime.hubspotintegration.service.HubSpotOAuthService;
import com.meetime.hubspotintegration.token.TokenStore;
import com.meetime.hubspotintegration.unit.util.MockWebClientHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HubSpotOAuthServiceTest {

    private static final String CLIENT_ID     = "cid";
    private static final String CLIENT_SECRET = "csec";
    private static final String REDIRECT_URI  = "http://cb";
    private static final String AUTH_URL      = "http://auth-url";
    private static final String TOKEN_URL     = "http://token-url";
    private static final String SCOPES        = "s1 s2";

    @Mock
    TokenStore tokenStore;

    @Test
    void buildAuthorizationUrlWithState_encodesAllParamsAndState() {
        var service = new HubSpotOAuthService(
                null, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );

        String state = "a b/c?d";
        String url = service.buildAuthorizationUrlWithState(state);

        assertTrue(url.startsWith(AUTH_URL + "?client_id=" + CLIENT_ID));
        assertTrue(url.contains("redirect_uri=http%3A%2F%2Fcb"));
        assertTrue(url.contains("scope=s1%20s2"));
        assertTrue(url.contains("state=a%20b%2Fc%3Fd"));
    }

    @Test
    void exchangeCodeForToken_successStoresTokenAndReturnsIt() {
        var mockResp = new OAuthTokenResponse();
        mockResp.setAccessToken("the-token");

        WebClient mockClient = MockWebClientHelper.mockFormPostResponse(
                TOKEN_URL, OAuthTokenResponse.class, mockResp
        );

        var service = new HubSpotOAuthService(
                mockClient, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );

        String token = service.exchangeCodeForToken("auth-code");
        assertEquals("the-token", token);
        verify(tokenStore).storeAccessToken("the-token");
    }

    @Test
    void exchangeCodeForToken_whenWebClientThrows_wrapsException() {
        WebClient badClient = mock(WebClient.class);
        when(badClient.post()).thenThrow(new RuntimeException("network failure"));

        var service = new HubSpotOAuthService(
                badClient, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );

        var ex = assertThrows(HubSpotIntegrationException.class,
                () -> service.exchangeCodeForToken("code"));

        assertTrue(ex.getMessage().contains("Error trying to obtain access token"));
        assertEquals("network failure", ex.getCause().getMessage());
    }

    @Test
    void exchangeCodeForToken_nullResponse_throws() {
        WebClient mockClient = mock(WebClient.class);
        var uriSpec    = mock(WebClient.RequestBodyUriSpec.class);
        var bodySpec   = mock(WebClient.RequestBodySpec.class);
        var headersSpec= mock(WebClient.RequestHeadersSpec.class);
        var respSpec   = mock(WebClient.ResponseSpec.class);

        when(mockClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(TOKEN_URL)).thenReturn(bodySpec);
        when(bodySpec.contentType(any())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(respSpec);
        when(respSpec.bodyToMono(OAuthTokenResponse.class)).thenReturn(Mono.empty());

        var service = new HubSpotOAuthService(
                mockClient, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );

        var ex = assertThrows(HubSpotIntegrationException.class,
                () -> service.exchangeCodeForToken("code"));
        assertTrue(ex.getMessage().contains("No access token received"));
    }

    @Test
    void getAccessToken_whenPresent_returnsValue() {
        when(tokenStore.getAccessToken()).thenReturn(Optional.of("xyz"));
        var service = new HubSpotOAuthService(
                null, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );

        assertEquals("xyz", service.getAccessToken());
    }

    @Test
    void getAccessToken_whenAbsent_throwsIllegalState() {
        when(tokenStore.getAccessToken()).thenReturn(Optional.empty());
        var service = new HubSpotOAuthService(
                null, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );

        var ex = assertThrows(IllegalStateException.class, service::getAccessToken);
        assertEquals("No access token available", ex.getMessage());
    }
}
