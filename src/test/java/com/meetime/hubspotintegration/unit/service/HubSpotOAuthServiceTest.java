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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private TokenStore tokenStore;

    @Test
    void buildAuthorizationUrlWithState_encodesAllParamsAndState() {
        HubSpotOAuthService service = new HubSpotOAuthService(
                null, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );

        String rawState = "a b/c?d";
        String url = service.buildAuthorizationUrlWithState(rawState);

        assertTrue(url.startsWith(AUTH_URL + "?client_id=" + CLIENT_ID));
        assertTrue(url.contains("redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8).replace("+", "%20")));
        assertTrue(url.contains("scope=" + "s1%20s2"));

        String expectedState = URLEncoder.encode(rawState, StandardCharsets.UTF_8).replace("+", "%20");
        assertTrue(url.contains("state=" + expectedState));
    }

    @Test
    void exchangeCodeForToken_successStoresBothTokensAndReturnsAccess() {
        OAuthTokenResponse resp = new OAuthTokenResponse("at123", "rt456", 999);

        WebClient mockClient = MockWebClientHelper.mockFormPostWithObjectResponse(
                TOKEN_URL, resp
        );

        HubSpotOAuthService service = new HubSpotOAuthService(
                mockClient, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );

        String token = service.exchangeCodeForToken("auth-code");
        assertEquals("at123", token);

        verify(tokenStore).storeAccessToken("at123");
        verify(tokenStore).storeRefreshToken("rt456");
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

        HubSpotOAuthService service = new HubSpotOAuthService(
                mockClient, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );

        HubSpotIntegrationException ex = assertThrows(
                HubSpotIntegrationException.class,
                () -> service.exchangeCodeForToken("code")
        );
        assertTrue(ex.getMessage().contains("No access token received"));
    }

    @Test
    void exchangeCodeForToken_webClientError_wrapsException() {
        WebClient badClient = mock(WebClient.class);
        when(badClient.post()).thenThrow(new RuntimeException("network down"));

        HubSpotOAuthService service = new HubSpotOAuthService(
                badClient, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );

        HubSpotIntegrationException ex = assertThrows(
                HubSpotIntegrationException.class,
                () -> service.exchangeCodeForToken("x")
        );
        assertTrue(ex.getMessage().contains("Error trying to obtain access token"));
        assertEquals("network down", ex.getCause().getMessage());
    }

    @Test
    void refreshAccessToken_successStoresBothTokensAndReturnsAccess() {
        OAuthTokenResponse resp = new OAuthTokenResponse("newAt", "newRt", 123);

        WebClient mockClient = MockWebClientHelper.mockFormPostWithObjectResponse(
                TOKEN_URL, resp
        );

        HubSpotOAuthService service = new HubSpotOAuthService(
                mockClient, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );

        String at = service.refreshAccessToken("oldRt");
        assertEquals("newAt", at);
        verify(tokenStore).storeAccessToken("newAt");
        verify(tokenStore).storeRefreshToken("newRt");
    }

    @Test
    void refreshAccessToken_nullOrInvalid_throws() {
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

        OAuthTokenResponse bad = new OAuthTokenResponse(null, "rt", 0);
        when(respSpec.bodyToMono(OAuthTokenResponse.class)).thenReturn(Mono.just(bad));

        HubSpotOAuthService service = new HubSpotOAuthService(
                mockClient, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );

        HubSpotIntegrationException ex = assertThrows(
                HubSpotIntegrationException.class,
                () -> service.refreshAccessToken("rt")
        );
        assertTrue(ex.getMessage().contains("Failed to refresh access token"));
    }

    @Test
    void getAccessToken_whenPresent_returnsIt() {
        when(tokenStore.getAccessToken()).thenReturn(Optional.of("z"));
        HubSpotOAuthService service = new HubSpotOAuthService(
                null, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );
        assertEquals("z", service.getAccessToken());
    }

    @Test
    void getAccessToken_whenAbsent_throws() {
        when(tokenStore.getAccessToken()).thenReturn(Optional.empty());
        HubSpotOAuthService service = new HubSpotOAuthService(
                null, tokenStore,
                CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                AUTH_URL, TOKEN_URL, SCOPES
        );
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                service::getAccessToken
        );
        assertEquals("No access token available", ex.getMessage());
    }
}
