package com.meetime.hubspotintegration.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspotintegration.dto.ContactDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class HubSpotClient {

    private static final String AUTH_URL = "https://app.hubspot.com/oauth/authorize";
    private static final String TOKEN_URL = "https://api.hubapi.com/oauth/v1/token";
    private static final String CONTACTS_URL = "https://api.hubapi.com/crm/v3/objects/contacts";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    private String accessToken; // Para simplificação

    public HubSpotClient(
            WebClient webClient,
            ObjectMapper objectMapper,
            @Value("${hubspot.client.id}") String clientId,
            @Value("${hubspot.client.secret}") String clientSecret,
            @Value("${hubspot.redirect.uri}") String redirectUri
    ) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
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

    public String exchangeToken(String code) {
        Map<String, String> formData = Map.of(
                "grant_type", "authorization_code",
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "code", code
        );

        return webClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(toUrlEncodedForm(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    accessToken = (String) response.get("access_token");
                    return accessToken;
                })
                .block(); // ainda simplificado
    }

    public String createContact(ContactDTO contactDTO) {
        try {
            String contactJson = objectMapper.writeValueAsString(contactDTO);

            return webClient.post()
                    .uri(CONTACTS_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(contactJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // simplificado
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar o contato", e);
        }
    }

    private String toUrlEncodedForm(Map<String, String> data) {
        StringBuilder builder = new StringBuilder();
        data.forEach((key, value) -> {
            if (!builder.isEmpty()) builder.append("&");
            builder.append(key).append("=").append(value);
        });
        return builder.toString();
    }
}