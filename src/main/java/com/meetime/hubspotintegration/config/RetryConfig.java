package com.meetime.hubspotintegration.config;

import com.meetime.hubspotintegration.exception.HubSpotIntegrationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
public class RetryConfig {

    @Bean
    public Retry retry() {
        return Retry.backoff(3, Duration.ofSeconds(2))
                .filter(throwable -> {
                    if (throwable instanceof HubSpotIntegrationException ex) {
                        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
                        return msg.contains("rate limit");
                    }
                    return false;
                });
    }
}
