package com.meetime.hubspotintegration.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTokenStoreTest {

    private InMemoryTokenStore tokenStore;

    @BeforeEach
    void setUp() {
        tokenStore = new InMemoryTokenStore();
    }

    @Test
    void getAccessToken_byDefault_shouldBeEmpty() {
        Optional<String> token = tokenStore.getAccessToken();
        assertTrue(token.isEmpty());
    }

    @Test
    void storeAccessToken_thenGetAccessToken_shouldReturnValue() {
        tokenStore.storeAccessToken("secret-token");
        Optional<String> token = tokenStore.getAccessToken();
        assertTrue(token.isPresent());
        assertEquals("secret-token", token.get());
    }

    @Test
    void storeAccessToken_null_thenGetAccessToken_shouldBeEmpty() {
        tokenStore.storeAccessToken(null);
        Optional<String> token = tokenStore.getAccessToken();
        assertTrue(token.isEmpty());
    }
}
