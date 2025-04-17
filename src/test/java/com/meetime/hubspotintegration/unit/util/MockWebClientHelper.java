package com.meetime.hubspotintegration.unit.util;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class MockWebClientHelper {

    public static WebClient mockJsonPostResponse(String responseBody) {
        return WebClient.builder()
                .exchangeFunction(clientRequest -> Mono.just(
                        ClientResponse
                                .create(HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body(responseBody)
                                .build()
                ))
                .build();
    }

    public static WebClient mockErrorJsonPostResponse(int status, String body) {
        return WebClient.builder()
                .exchangeFunction(clientRequest -> Mono.just(
                        ClientResponse
                                .create(HttpStatus.valueOf(status))
                                .header("Content-Type", "application/json")
                                .body(body)
                                .build()
                ))
                .build();
    }

    public static WebClient mock429RateLimitResponse() {
        return mockErrorJsonPostResponse(429, "Rate limit exceeded");
    }

    public static WebClient mockFormPostWithObjectResponse(String expectedUrl, Object responseBody) {
        WebClient mockClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(mockClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(expectedUrl)).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(bodySpec);
        when(bodySpec.bodyValue(anyString())).thenReturn((WebClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(any(Class.class)))
                .thenAnswer(invocation -> {
                    Class<?> type = invocation.getArgument(0);
                    if (type.isInstance(responseBody)) {
                        return Mono.just(type.cast(responseBody));
                    }
                    return Mono.empty();
                });

        return mockClient;
    }
}
