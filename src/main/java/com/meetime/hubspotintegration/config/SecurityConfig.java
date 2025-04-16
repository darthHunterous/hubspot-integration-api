package com.meetime.hubspotintegration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final String contactsToken;
    private final ObjectMapper objectMapper;

    public SecurityConfig(@Value("${contacts.auth.token}") String contactsToken, ObjectMapper objectMapper) {
        this.contactsToken = contactsToken;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilter(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/url", "/auth/callback").permitAll()
                        .requestMatchers(HttpMethod.POST, "/contacts").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new TokenAuthenticationFilter(contactsToken, objectMapper),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
