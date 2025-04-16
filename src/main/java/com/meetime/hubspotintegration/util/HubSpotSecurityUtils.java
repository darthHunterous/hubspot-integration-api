package com.meetime.hubspotintegration.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class HubSpotSecurityUtils {

    @Value("${hubspot.client.secret}")
    private String CLIENT_SECRET;

    public boolean isValidSignature(String payload, String signatureFromHeader) {
        try {
            String baseString = CLIENT_SECRET + payload;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(baseString.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equalsIgnoreCase(signatureFromHeader);
        } catch (Exception e) {
            return false;
        }
    }
}