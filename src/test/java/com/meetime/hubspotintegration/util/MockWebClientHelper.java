package com.meetime.hubspotintegration.util;

import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class MockWebClientHelper {

    public static WebClient mockPostJsonResponse(String expectedUri, String expectedBody, String expectedToken, String responseBody) {
        WebClient webClient = Mockito.mock(WebClient.class);

        WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = Mockito.mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(expectedUri)).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.header("Authorization", "Bearer " + expectedToken)).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.bodyValue(expectedBody)).thenReturn(requestHeadersSpec);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseBody));

        return webClient;
    }

    public static WebClient mockPostError(String expectedUri, String expectedBody, String expectedToken, RuntimeException exToThrow) {
        WebClient webClient = Mockito.mock(WebClient.class);

        WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = Mockito.mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(expectedUri)).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.header("Authorization", "Bearer " + expectedToken)).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.bodyValue(expectedBody)).thenReturn(requestHeadersSpec);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(String.class)).thenThrow(exToThrow);

        return webClient;
    }
}
