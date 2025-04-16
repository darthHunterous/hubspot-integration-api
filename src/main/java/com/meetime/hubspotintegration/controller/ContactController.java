package com.meetime.hubspotintegration.controller;

import com.meetime.hubspotintegration.dto.ContactDTO;
import com.meetime.hubspotintegration.service.ContactService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @SecurityRequirement(name = "BearerAuth")
    @PostMapping(consumes = "application/json", produces = "application/json")
    public String createContact(@Valid @RequestBody ContactDTO contactDTO) {
        return contactService.syncContactToHubSpot(contactDTO);
    }
}
