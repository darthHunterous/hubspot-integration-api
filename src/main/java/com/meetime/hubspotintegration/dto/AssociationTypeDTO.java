package com.meetime.hubspotintegration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssociationTypeDTO {

    @NotNull
    @JsonProperty("associationCategory")
    @Schema(description = "Association category", example = "HUBSPOT_DEFINED")
    private AssociationCategory associationCategory;

    @NotNull
    @JsonProperty("associationTypeId")
    @Schema(description = "Association type ID provided by Hubspot", example = "123456")
    private Integer associationTypeId;

    private enum AssociationCategory {
        HUBSPOT_DEFINED,
        USER_DEFINED,
        INTEGRATOR_DEFINED
    }
}
