package com.meetime.hubspotintegration.unit.service;

import com.meetime.hubspotintegration.client.HubSpotClient;
import com.meetime.hubspotintegration.dto.ContactDTO;
import com.meetime.hubspotintegration.exception.HubSpotIntegrationException;
import com.meetime.hubspotintegration.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private HubSpotClient hubSpotClient;

    @InjectMocks
    private ContactService contactService;

    private ContactDTO dummyDto;

    @BeforeEach
    void setUp() {
        dummyDto = new ContactDTO();
    }

    @Test
    void whenHubSpotClientSucceeds_thenReturnResponse() {
        // Arrange
        String expected = "{\"id\":\"123\"}";
        when(hubSpotClient.createContact(dummyDto)).thenReturn(expected);

        // Act
        String actual = contactService.syncContactToHubSpot(dummyDto);

        // Assert
        assertSame(expected, actual);
        verify(hubSpotClient).createContact(dummyDto);
    }

    @Test
    void whenHubSpotClientThrows_thenWrapInHubSpotIntegrationException() {
        // Arrange
        RuntimeException cause = new RuntimeException("HTTP 500");
        when(hubSpotClient.createContact(dummyDto)).thenThrow(cause);

        // Act & Assert
        HubSpotIntegrationException ex = assertThrows(
                HubSpotIntegrationException.class,
                () -> contactService.syncContactToHubSpot(dummyDto)
        );
        assertEquals("Failed to create contact on HubSpot", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
