package com.example.concurrency.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    private final Logger log = LoggerFactory.getLogger("defaultWebClient");
    @Bean
    public WebClient defaultWebClient() {
        return WebClient
                .builder()
                .baseUrl("http://localhost:8080")
                .build();
    }
}