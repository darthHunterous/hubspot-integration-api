package com.meetime.hubspotintegration.integration;

import com.meetime.hubspotintegration.unit.config.MockedBeansConfig;
import com.meetime.hubspotintegration.service.WebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MockedBeansConfig.class)
@ActiveProfiles("test")
class WebhookIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private WebhookService webhookService;

    private static final String CLIENT_SECRET = "dummy";

    private String generateSignatureV1(String payload) throws Exception {
        String base = CLIENT_SECRET + payload;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(base.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Test
    void shouldReturn200IfSignatureIsValid() throws Exception {
        String payload = "[{\"eventId\":1,\"subscriptionType\":\"contact.creation\",\"objectId\":123}]";
        String signature = generateSignatureV1(payload);
        String timestamp = String.valueOf(Instant.now().toEpochMilli());

        webTestClient.post()
                .uri("/webhook/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-HubSpot-Signature", signature)
                .header("X-HubSpot-Request-Timestamp", timestamp)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldReturn401IfSignatureIsInvalid() {
        String payload = "[{\"eventId\":2}]";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        webTestClient.post()
                .uri("/webhook/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-HubSpot-Signature", "assinatura-invalida")
                .header("X-HubSpot-Request-Timestamp", timestamp)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
