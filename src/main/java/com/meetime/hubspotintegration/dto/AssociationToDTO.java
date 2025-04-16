package com.meetime.hubspotintegration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
@Schema(description = "Association object reference")
public class AssociationToDTO {

    @NotBlank
    @JsonProperty("id")
    @Schema(description = "Association object Id", example = "123456")
    private String id;
}
