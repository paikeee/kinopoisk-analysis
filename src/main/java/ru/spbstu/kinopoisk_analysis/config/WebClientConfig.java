package ru.spbstu.kinopoisk_analysis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${kinopoisk.url.main}")
    private String url;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .exchangeStrategies(
                        ExchangeStrategies.builder().codecs(configurer ->
                                configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)).build()
                )
                .baseUrl(url)
                .build();
    }
}
