package com.meetime.hubspotintegration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OAuth2Config {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}