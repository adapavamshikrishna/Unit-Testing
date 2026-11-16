package com.company.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient departmentRestClient(
            RestClient.Builder builder, ServiceUrlProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getDepartmentConnectTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getDepartmentReadTimeoutMs()));

        return builder
                .requestFactory(requestFactory)
                .build();
    }
}
