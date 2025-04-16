package com.meetime.hubspotintegration.service;

import com.meetime.hubspotintegration.dto.OAuthTokenResponse;
import com.meetime.hubspotintegration.exception.HubSpotIntegrationException;
import com.meetime.hubspotintegration.token.TokenStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HubSpotOAuthService {

    private final WebClient webClient;
    private final TokenStore tokenStore;

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String authUrl;
    private final String tokenUrl;
    private final String scopes;

    public HubSpotOAuthService(
            WebClient webClient,
            TokenStore tokenStore,
            @Value("${hubspot.client.id}") String clientId,
            @Value("${hubspot.client.secret}") String clientSecret,
            @Value("${hubspot.redirect.uri}") String redirectUri,
            @Value("${hubspot.auth.url}") String authUrl,
            @Value("${hubspot.token.url}") String tokenUrl,
            @Value("${hubspot.scopes}") String scopes
    ) {
        this.webClient = webClient;
        this.tokenStore = tokenStore;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.authUrl = authUrl;
        this.tokenUrl = tokenUrl;
        this.scopes = scopes;
    }

    public String buildAuthorizationUrlWithState(String state) {
        return authUrl +
                "?client_id="   + urlEncode(clientId) +
                "&redirect_uri="+ urlEncode(redirectUri) +
                "&scope="       + urlEncode(scopes) +
                "&state="       + urlEncode(state);
    }

    public String exchangeCodeForToken(String code) {
        Map<String, String> form = Map.of(
                "grant_type", "authorization_code",
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "code", code
        );

        OAuthTokenResponse response;
        try {
            response = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(toUrlEncodedForm(form))
                    .retrieve()
                    .bodyToMono(OAuthTokenResponse.class)
                    .block();
        } catch (Exception e) {
            throw new HubSpotIntegrationException("Error trying to obtain access token from HubSpot", e);
        }

        if (response == null || response.getAccessToken() == null) {
            throw new HubSpotIntegrationException("No access token received from HubSpot", null);
        }

        String accessToken = response.getAccessToken();
        tokenStore.storeAccessToken(accessToken);
        return accessToken;
    }

    public String getAccessToken() {
        return tokenStore.getAccessToken()
                .orElseThrow(() -> new IllegalStateException("No access token available"));
    }

    private String toUrlEncodedForm(Map<String, String> data) {
        return data.entrySet().stream()
                .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
