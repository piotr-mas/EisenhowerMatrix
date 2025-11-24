package com.piotr.matrix.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AuthConfig {

    @Bean
    public WebClient.Builder userWebClientBuilder() {
        return WebClient.builder().baseUrl("http://ms-user");
    }
}
