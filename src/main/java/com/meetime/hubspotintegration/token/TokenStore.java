package com.meetime.hubspotintegration.token;

import java.util.Optional;

public interface TokenStore {
    void storeAccessToken(String token);
    Optional<String> getAccessToken();
}
