package com.meetime.hubspotintegration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Association from the contact to another HubSpot object")
public class ContactAssociationDTO {

    @NotNull
    @JsonProperty("to")
    @Schema(description = "Object to associate contact with")
    private AssociationToDTO to;

    @NotNull
    @JsonProperty("types")
    @Schema(description = "Association types (category and type")
    private List<AssociationTypeDTO> types;
}
