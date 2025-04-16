package com.meetime.hubspotintegration.config;

import com.meetime.hubspotintegration.client.HubSpotClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockedBeansConfig {

    @Bean
    public HubSpotClient hubSpotClient() {
        return Mockito.mock(HubSpotClient.class);
    }
}
