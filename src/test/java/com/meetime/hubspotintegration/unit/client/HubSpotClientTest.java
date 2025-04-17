package com.meetime.hubspotintegration.unit.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspotintegration.client.HubSpotClient;
import com.meetime.hubspotintegration.dto.ContactDTO;
import com.meetime.hubspotintegration.exception.HubSpotIntegrationException;
import com.meetime.hubspotintegration.service.HubSpotOAuthService;
import com.meetime.hubspotintegration.unit.util.MockWebClientHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import reactor.util.retry.Retry;

@ExtendWith(MockitoExtension.class)
class HubSpotClientTest {

    private static final String TOKEN = "access-token";
    private static final String CONTACTS_URL = "https://api.hubapi.com/crm/v3/objects/contacts";

    @Mock
    private WebClient webClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HubSpotOAuthService oAuthService;

    private HubSpotClient hubSpotClient;

    private final Retry noRetry = Retry.max(0);

    @BeforeEach
    void setUp() {
        hubSpotClient = new HubSpotClient(webClient, objectMapper, oAuthService, noRetry, CONTACTS_URL);
    }

    @Test
    void createContact_successfulRequest_shouldReturnResponseBody() throws JsonProcessingException {
        // Arrange
        ContactDTO dto = new ContactDTO();
        String jsonBody = "{\"properties\":{\"email\":\"a@b.com\"}}";
        String expectedResponse = "{\"id\":\"123\"}";

        when(oAuthService.getAccessToken()).thenReturn(TOKEN);
        when(objectMapper.writeValueAsString(dto)).thenReturn(jsonBody);

        WebClient mock = MockWebClientHelper.mockJsonPostResponse(expectedResponse);
        hubSpotClient = new HubSpotClient(mock, objectMapper, oAuthService, noRetry, CONTACTS_URL);

        // Act
        String result = hubSpotClient.createContact(dto);

        // Assert
        assertEquals(expectedResponse, result);
    }

    @Test
    void createContact_whenJsonProcessingFails_shouldThrowRuntimeException() throws JsonProcessingException {
        // Arrange
        ContactDTO dto = new ContactDTO();
        when(objectMapper.writeValueAsString(dto)).thenThrow(new JsonProcessingException("fail") {});
        when(oAuthService.getAccessToken()).thenReturn(TOKEN);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> hubSpotClient.createContact(dto));
    }

    @Test
    void createContact_whenHubSpotReturns429_shouldRetryAndThrowAfterMaxRetries() throws JsonProcessingException {
        // Arrange
        ContactDTO dto = new ContactDTO();
        String jsonBody = "{\"properties\":{\"email\":\"a@b.com\"}}";
        when(oAuthService.getAccessToken()).thenReturn(TOKEN);
        when(objectMapper.writeValueAsString(dto)).thenReturn(jsonBody);

        WebClient mock = MockWebClientHelper.mock429RateLimitResponse();
        hubSpotClient = new HubSpotClient(mock, objectMapper, oAuthService, noRetry, CONTACTS_URL);

        // Act & Assert
        Exception ex = assertThrows(RuntimeException.class, () -> hubSpotClient.createContact(dto));
        assertInstanceOf(HubSpotIntegrationException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("Rate limit exceeded"));
    }

    @Test
    void createContact_whenHubSpotReturns400_shouldThrowIntegrationException() throws JsonProcessingException {
        // Arrange
        ContactDTO dto = new ContactDTO();
        String jsonBody = "{\"properties\":{\"email\":\"a@b.com\"}}";
        when(oAuthService.getAccessToken()).thenReturn(TOKEN);
        when(objectMapper.writeValueAsString(dto)).thenReturn(jsonBody);

        WebClient mock = MockWebClientHelper.mockErrorJsonPostResponse(400, "Bad Request");
        hubSpotClient = new HubSpotClient(mock, objectMapper, oAuthService, noRetry, CONTACTS_URL);

        // Act & Assert
        Exception ex = assertThrows(RuntimeException.class, () -> hubSpotClient.createContact(dto));
        assertInstanceOf(HubSpotIntegrationException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("HubSpot error:"));
    }
}
