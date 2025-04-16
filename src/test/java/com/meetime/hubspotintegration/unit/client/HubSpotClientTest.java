package com.meetime.hubspotintegration.unit.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspotintegration.client.HubSpotClient;
import com.meetime.hubspotintegration.dto.ContactDTO;
import com.meetime.hubspotintegration.dto.ContactPropertiesDTO;
import com.meetime.hubspotintegration.service.HubSpotOAuthService;
import com.meetime.hubspotintegration.unit.util.MockWebClientHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HubSpotClientTest {

    private HubSpotClient hubSpotClient;
    private HubSpotOAuthService oAuthService;
    private ObjectMapper objectMapper;

    private static final String TOKEN = "dummy-token";
    private static final String URI = "https://api.hubapi.com/crm/v3/objects/contacts";
    private static final String JSON_BODY = "{\"properties\":{\"email\":\"test@example.com\",\"firstname\":\"John\",\"lastname\":\"Doe\"}}";
    private static final String RESPONSE = "{\"result\":\"ok\"}";

    private ContactDTO createMockContact() {
        ContactDTO dto = new ContactDTO();
        ContactPropertiesDTO properties = new ContactPropertiesDTO();

        properties.setEmail("test@example.com");
        properties.setFirstname("John");
        properties.setLastname("Doe");
        dto.setProperties(properties);

        return dto;
    }

    @BeforeEach
    void setup() {
        objectMapper = Mockito.mock(ObjectMapper.class);
        oAuthService = Mockito.mock(HubSpotOAuthService.class);
    }

    @Test
    void shouldCreateContactSuccessfully() throws Exception {
        // Arrange
        ContactDTO dto = createMockContact();

        WebClient webClient = MockWebClientHelper.mockPostJsonResponse(
                URI,
                JSON_BODY,
                TOKEN,
                RESPONSE
        );

        when(oAuthService.getAccessToken()).thenReturn(TOKEN);
        when(objectMapper.writeValueAsString(dto)).thenReturn(JSON_BODY);

        hubSpotClient = new HubSpotClient(webClient, objectMapper, oAuthService);

        // Act
        String result = hubSpotClient.createContact(dto);

        // Assert
        assertEquals(RESPONSE, result);
        verify(oAuthService).getAccessToken();
        verify(objectMapper).writeValueAsString(dto);
    }

    @Test
    void shouldThrowRuntimeExceptionIfSerializationFails() throws Exception {
        // Arrange
        ContactDTO dto = createMockContact();
        WebClient webClient = mock(WebClient.class);

        when(objectMapper.writeValueAsString(dto)).thenThrow(new JsonProcessingException("fail") {});
        hubSpotClient = new HubSpotClient(webClient, objectMapper, oAuthService);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> hubSpotClient.createContact(dto));
        assertTrue(ex.getMessage().contains("Error serializing ContactDTO"));
    }
}
