package com.meetime.hubspotintegration.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OAuthIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturnAuthorizationUrl() {
        webTestClient.get()
                .uri("/auth/url")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("https://app.hubspot.com/oauth/authorize");
                });
    }
}
