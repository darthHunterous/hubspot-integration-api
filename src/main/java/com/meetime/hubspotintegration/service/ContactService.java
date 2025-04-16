package com.meetime.hubspotintegration.service;

import com.meetime.hubspotintegration.client.HubSpotClient;
import com.meetime.hubspotintegration.dto.ContactDTO;
import com.meetime.hubspotintegration.exception.HubSpotIntegrationException;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    private final HubSpotClient hubSpotClient;

    public ContactService(HubSpotClient hubSpotClient) {
        this.hubSpotClient = hubSpotClient;
    }

    public String syncContactToHubSpot(ContactDTO dto) {
        try {
            return hubSpotClient.createContact(dto);
        } catch (Exception e) {
            throw new HubSpotIntegrationException("Failed to create contact on HubSpot", e);
        }
    }
}