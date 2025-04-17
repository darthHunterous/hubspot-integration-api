package com.meetime.hubspotintegration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
public class RetryConfig {

    @Bean
    public Retry hubspotRetry() {
        return Retry.backoff(3, Duration.ofSeconds(2));
    }
}
