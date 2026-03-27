package com.example.sbapp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(IdpProperties.class)
public class AppConfig {
    @Bean
    RestClient idpRestClient(RestClient.Builder builder, IdpProperties props) {
        return builder.baseUrl(props.internalBaseUrl()).build();
    }
}
