package com.meetime.hubspotintegration.service;

import com.meetime.hubspotintegration.client.HubSpotClient;
import com.meetime.hubspotintegration.dto.ContactDTO;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    private final HubSpotClient hubSpotClient;

    public ContactService(HubSpotClient hubSpotClient) {
        this.hubSpotClient = hubSpotClient;
    }

    public String createContact(ContactDTO dto) {
        return hubSpotClient.createContact(dto);
    }
}