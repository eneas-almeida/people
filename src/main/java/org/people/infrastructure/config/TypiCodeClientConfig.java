package org.people.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TypiCodeClientConfig {

    @Bean
    WebClient typicodeWebClient(WebClient.Builder builder,
            @Value("${client.typicode.base-url}") String baseUrl) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs()
                        .maxInMemorySize(5242880))
                .build();

        return builder
                .baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .build();
    }
}
