package com.meetime.hubspotintegration.controller;

import com.meetime.hubspotintegration.dto.ContactDTO;
import com.meetime.hubspotintegration.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contacts")
class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public String createContact(@Valid @RequestBody ContactDTO contactDTO) {
        return contactService.createContact(contactDTO);
    }
}
