package com.meetime.hubspotintegration.token;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class InMemoryTokenStore implements TokenStore {

    private final AtomicReference<String> token = new AtomicReference<>();

    @Override
    public void storeAccessToken(String accessToken) {
        token.set(accessToken);
    }

    @Override
    public Optional<String> getAccessToken() {
        return Optional.ofNullable(token.get());
    }
}
