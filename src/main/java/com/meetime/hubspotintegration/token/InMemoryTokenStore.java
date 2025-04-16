package com.meetime.hubspotintegration.token;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InMemoryTokenStore implements TokenStore {

    private volatile String accessToken;
    private volatile String refreshToken;

    @Override
    public Optional<String> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    @Override
    public void storeAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public Optional<String> getRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    @Override
    public void storeRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
