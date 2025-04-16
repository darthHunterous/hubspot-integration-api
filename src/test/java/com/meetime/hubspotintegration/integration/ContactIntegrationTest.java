package com.meetime.hubspotintegration.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspotintegration.client.HubSpotClient;
import com.meetime.hubspotintegration.config.MockedBeansConfig;
import com.meetime.hubspotintegration.dto.ContactDTO;
import com.meetime.hubspotintegration.dto.ContactPropertiesDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MockedBeansConfig.class)
@ActiveProfiles("test")
class ContactIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private HubSpotClient hubSpotClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String VALID_TOKEN = "contacts-auth-token";

    @Test
    void shouldReturn401IfNoTokenProvided() {
        ContactPropertiesDTO properties = new ContactPropertiesDTO();
        properties.setEmail("noauth@example.com");
        properties.setFirstname("John");
        properties.setLastname("Doe");

        ContactDTO dto = new ContactDTO();
        dto.setProperties(properties);

        webTestClient.post()
                .uri("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn400IfContactIsInvalid() {
        ContactDTO dto = new ContactDTO();

        webTestClient.post()
                .uri("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + VALID_TOKEN)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldAcceptValidContactAndReturn200() {
        // Arrange
        ContactPropertiesDTO properties = new ContactPropertiesDTO();
        properties.setEmail("noauth@example.com");
        properties.setFirstname("John");
        properties.setLastname("Doe");

        ContactDTO dto = new ContactDTO();
        dto.setProperties(properties);

        Mockito.when(hubSpotClient.createContact(Mockito.any()))
                .thenReturn("hubspot-id-123");

        // Act + Assert
        webTestClient.post()
                .uri("/contacts")
                .header("Authorization", "Bearer " + VALID_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();
    }
}
