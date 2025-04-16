package com.meetime.hubspotintegration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO to create a new contact on HubSpot, with properties and associations")
public class ContactDTO {

    @NotNull
    @Valid
    @JsonProperty("properties")
    @Schema(description = "Contact properties")
    public ContactPropertiesDTO properties;

    @JsonProperty("associations")
    @Schema(description = "Association list with other HubSpot objects")
    public List<ContactAssociationDTO> associations;
}