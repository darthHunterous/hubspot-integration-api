package com.meetime.hubspotintegration.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ApiError {

    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String timestamp;

    public ApiError(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now().toString();
    }

    @JsonCreator
    public ApiError(
            @JsonProperty("status") int status,
            @JsonProperty("error") String error,
            @JsonProperty("message") String message,
            @JsonProperty("path") String path,
            @JsonProperty("timestamp") String timestamp
    ) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }
}
