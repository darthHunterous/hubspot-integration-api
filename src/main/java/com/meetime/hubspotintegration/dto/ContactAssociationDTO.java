package com.meetime.hubspotintegration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;


@Schema(description = "Association from the contact to another HubSpot object")
public class ContactAssociationDTO {

    @NotNull
    @JsonProperty("to")
    @Schema(description = "Object to associate contact with")
    public AssociationToDTO to;

    @NotNull
    @JsonProperty("types")
    @Schema(description = "Association types (category and type")
    public List<AssociationTypeDTO> types;
}
