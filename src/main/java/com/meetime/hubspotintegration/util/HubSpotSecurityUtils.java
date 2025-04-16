package com.meetime.hubspotintegration.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class HubSpotSecurityUtils {

    private static final String CRYPTO_METHOD = "SHA-256";

    private final String clientSecret;

    public HubSpotSecurityUtils(@Value("${hubspot.client.secret}") String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public boolean isValidSignature(String payload, String signatureFromHeader) {
        if (payload == null || signatureFromHeader == null) {
            return false;
        }
        String computed = computeSignature(payload);
        return computed.equalsIgnoreCase(signatureFromHeader);
    }

    private String computeSignature(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance(CRYPTO_METHOD);
            byte[] hash = digest.digest((clientSecret + payload).getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(CRYPTO_METHOD + " algorithm not available", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}