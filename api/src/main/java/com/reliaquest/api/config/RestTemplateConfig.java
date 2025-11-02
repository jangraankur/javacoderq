package com.reliaquest.api.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for setting up HTTP client components.
 * This class configures the RestTemplate bean used for making
 * HTTP requests to the mock employee API.
 *
 * <p>The RestTemplate is configured with appropriate timeout values
 * to prevent hanging requests and ensure responsive error handling.</p>
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates and configures a RestTemplate bean for HTTP communication.
     *
     * <p>Configuration includes:</p>
     * <ul>
     *   <li>Connect timeout: 10 seconds - maximum time to establish a connection</li>
     *   <li>Read timeout: 10 seconds - maximum time to wait for a response</li>
     * </ul>
     *
     * <p>These timeout values provide a balance between allowing sufficient time
     * for requests to complete and failing fast when the service is unavailable.</p>
     *
     * @param builder the RestTemplateBuilder provided by Spring Boot
     * @return a configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                // Set connection timeout - time to establish connection to the server
                .setConnectTimeout(Duration.ofSeconds(10))
                // Set read timeout - time to wait for response after connection is established
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}

