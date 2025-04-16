package com.meetime.hubspotintegration.service;

import com.meetime.hubspotintegration.token.TokenStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class HubSpotOAuthService {

    private static final String AUTH_URL = "https://app.hubspot.com/oauth/authorize";
    private static final String TOKEN_URL = "https://api.hubapi.com/oauth/v1/token";

    private final WebClient webClient;
    private final TokenStore tokenStore;

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public HubSpotOAuthService(
            WebClient webClient,
            TokenStore tokenStore,
            @Value("${hubspot.client.id}") String clientId,
            @Value("${hubspot.client.secret}") String clientSecret,
            @Value("${hubspot.redirect.uri}") String redirectUri
    ) {
        this.webClient = webClient;
        this.tokenStore = tokenStore;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public String buildAuthorizationUrl() {
        return AUTH_URL +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=crm.objects.contacts.write%20crm.objects.contacts.read";
    }

    public String exchangeCodeForToken(String code) {
        Map<String, String> form = Map.of(
                "grant_type", "authorization_code",
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "code", code
        );

        String accessToken = webClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(toUrlEncodedForm(form))
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> (String) resp.get("access_token"))
                .block();

        tokenStore.storeAccessToken(accessToken);
        return accessToken;
    }

    public String getAccessToken() {
        return tokenStore.getAccessToken()
                .orElseThrow(() -> new IllegalStateException("No access token available"));
    }

    private String toUrlEncodedForm(Map<String, String> data) {
        StringBuilder builder = new StringBuilder();
        data.forEach((k, v) -> {
            if (!builder.isEmpty()) builder.append("&");
            builder.append(k).append("=").append(v);
        });
        return builder.toString();
    }
}
