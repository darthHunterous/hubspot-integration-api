package com.meetime.hubspotintegration.unit.util;

import com.meetime.hubspotintegration.util.HubSpotSecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;

class HubSpotSecurityUtilsTest {

    private HubSpotSecurityUtils utils;

    @BeforeEach
    void setUp() {
        utils = new HubSpotSecurityUtils("client-secret");
    }

    @Test
    void isValidSignature_withCorrectSignature_returnsTrue() throws Exception {
        String payload = "payload";
        String secret  = "client-secret";
        String base = secret + payload;

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(base.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        String expectedHex = sb.toString();

        assertTrue(utils.isValidSignature(payload, expectedHex));
    }

    @Test
    void isValidSignature_withWrongSignature_returnsFalse() {
        assertFalse(utils.isValidSignature("payload", "wrong-signature"));
    }

    @Test
    void isValidSignature_signatureCaseInsensitive() throws Exception {
        String payload = "payload";
        String secret  = "client-secret";
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest((secret + payload).getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        String lower = sb.toString();
        String upper = lower.toUpperCase();

        assertTrue(utils.isValidSignature(payload, lower));
        assertTrue(utils.isValidSignature(payload, upper));
    }

    @Test
    void isValidSignature_nullOrEmptyInputs_returnsFalse() {
        assertFalse(utils.isValidSignature("payload", null));
        assertFalse(utils.isValidSignature(null, "wrong-signature"));
    }
}
