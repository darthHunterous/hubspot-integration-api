package com.meetime.hubspotintegration.unit.token;

import com.meetime.hubspotintegration.token.InMemoryTokenStore;
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
        tokenStore.storeAccessToken("access-123");
        Optional<String> token = tokenStore.getAccessToken();
        assertTrue(token.isPresent());
        assertEquals("access-123", token.get());
    }

    @Test
    void storeAccessToken_null_thenGetAccessToken_shouldBeEmpty() {
        tokenStore.storeAccessToken(null);
        Optional<String> token = tokenStore.getAccessToken();
        assertTrue(token.isEmpty());
    }

    @Test
    void getRefreshToken_byDefault_shouldBeEmpty() {
        Optional<String> token = tokenStore.getRefreshToken();
        assertTrue(token.isEmpty());
    }

    @Test
    void storeRefreshToken_thenGetRefreshToken_shouldReturnValue() {
        tokenStore.storeRefreshToken("refresh-456");
        Optional<String> token = tokenStore.getRefreshToken();
        assertTrue(token.isPresent());
        assertEquals("refresh-456", token.get());
    }

    @Test
    void storeRefreshToken_null_thenGetRefreshToken_shouldBeEmpty() {
        tokenStore.storeRefreshToken(null);
        Optional<String> token = tokenStore.getRefreshToken();
        assertTrue(token.isEmpty());
    }
}
