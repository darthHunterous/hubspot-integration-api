package com.meetime.hubspotintegration.exception;

import java.time.Instant;

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

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public String getTimestamp() { return timestamp; }
}
