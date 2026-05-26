package com.krishimitra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @Bean
    public WebClient aiServiceClient() {
        return WebClient.builder()
            .baseUrl(aiServiceUrl)
            .build();
    }
}
