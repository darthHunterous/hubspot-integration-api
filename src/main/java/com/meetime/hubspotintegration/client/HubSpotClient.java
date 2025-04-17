package com.meetime.hubspotintegration.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspotintegration.dto.ContactDTO;
import com.meetime.hubspotintegration.exception.HubSpotIntegrationException;
import com.meetime.hubspotintegration.service.HubSpotOAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class HubSpotClient {

    private static final Logger log = LoggerFactory.getLogger(HubSpotClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final HubSpotOAuthService oAuthService;
    private final Retry retry;
    private final String contactsUrl;

    public HubSpotClient(WebClient webClient,
                         ObjectMapper objectMapper,
                         HubSpotOAuthService oAuthService,
                         Retry retry,
                         @Value("${hubspot.contacts.url}") String contactsUrl) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.oAuthService = oAuthService;
        this.retry = retry;
        this.contactsUrl = contactsUrl;
    }

    public String createContact(ContactDTO contactDTO) {
        String token = oAuthService.getAccessToken();
        String body = toJson(contactDTO);

        ResponseEntity<String> response = sendCreateContactRequest(token, body);

        logRateLimitHeaders(response);
        return response.getBody();
    }

    private ResponseEntity<String> sendCreateContactRequest(String token, String bodyJson) {
        return webClient.post()
                .uri(contactsUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bodyJson)
                .retrieve()
                .onStatus(this::isRateLimitError, this::handleRateLimitError)
                .onStatus(this::isClientOrServerError, this::handleHubSpotError)
                .toEntity(String.class)
                .retryWhen(retry)
                .block();
    }

    private boolean isRateLimitError(HttpStatusCode status) {
        return status.value() == 429;
    }

    private boolean isClientOrServerError(HttpStatusCode status) {
        return status.is4xxClientError() || status.is5xxServerError();
    }

    private Mono<HubSpotIntegrationException> handleRateLimitError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .map(body -> {
                    log.warn("Rate limit exceeded: {}", body);
                    return new HubSpotIntegrationException("Rate limit exceeded: " + body, null);
                });
    }

    private Mono<HubSpotIntegrationException> handleHubSpotError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .map(body -> {
                    log.error("HubSpot error response ({}): {}", response.statusCode(), body);
                    return new HubSpotIntegrationException("HubSpot error: " + body, null);
                });
    }

    private String toJson(ContactDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing ContactDTO", e);
        }
    }

    private void logRateLimitHeaders(ResponseEntity<?> response) {
        var headers = response.getHeaders();

        String daily = headers.getFirst("X-HubSpot-RateLimit-Daily");
        String remaining = headers.getFirst("X-HubSpot-RateLimit-Remaining");
        String reset = headers.getFirst("X-HubSpot-RateLimit-Reset");

        if (daily != null || remaining != null || reset != null) {
            log.info("HubSpot Rate Limit - Remaining: {}, Daily: {}, Reset: {}",
                    remaining, daily, reset);
        }
    }
}