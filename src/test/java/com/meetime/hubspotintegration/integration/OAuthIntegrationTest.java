package com.meetime.hubspotintegration.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OAuthIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturnAuthorizationUrlWithStateAndEncodedParams() {
        EntityExchangeResult<String> result = webTestClient.get()
                .uri("/auth/url")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult();

        String url = result.getResponseBody();
        assertThat(url).isNotNull();
        assertThat(url).startsWith("https://app.hubspot.com/oauth/authorize?");
        assertThat(url).contains("scope=crm.objects.contacts.write%20crm.objects.contacts.read");
        assertThat(url).containsPattern("([?&])state=[^&]+");
    }
}
