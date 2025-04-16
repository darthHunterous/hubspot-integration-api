package com.meetime.hubspotintegration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Contact main properties")
public class ContactPropertiesDTO {
    @Email
    @NotBlank
    @JsonProperty("email")
    @Schema(description = "Contact email", example = "email@example.com")
    private String email;

    @NotBlank
    @JsonProperty("firstname")
    @Schema(description = "Contact First Name", example = "FirstName")
    private String firstname;

    @NotBlank
    @JsonProperty("lastname")
    @Schema(description = "Contact Last Name", example = "LastName")
    private String lastname;
}
