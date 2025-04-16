package com.meetime.hubspotintegration.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspotintegration.dto.ContactDTO;
import com.meetime.hubspotintegration.service.HubSpotOAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class HubSpotClient {

    private static final String CONTACTS_URL = "https://api.hubapi.com/crm/v3/objects/contacts";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final HubSpotOAuthService oAuthService;

    public HubSpotClient(WebClient webClient,
                         ObjectMapper objectMapper,
                         HubSpotOAuthService oAuthService) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.oAuthService = oAuthService;
    }

    public String createContact(ContactDTO contactDTO) {
        try {
            String token = oAuthService.getAccessToken(); // busca o token válido
            String body = objectMapper.writeValueAsString(contactDTO);

            return webClient.post()
                    .uri(CONTACTS_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar o DTO do contato", e);
        }
    }
}