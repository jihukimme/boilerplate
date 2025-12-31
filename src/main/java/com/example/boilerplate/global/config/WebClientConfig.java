package com.example.boilerplate.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // 추후 연결 필요시 baseUrl 연결 설정
    private final String baseUrl = "http://localhost:8080"; // 임시 기본값 설정

    @Bean
    public WebClient externalWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}