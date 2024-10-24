package ru.spbstu.kinopoisk_analysis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ApiService {

    private final WebClient webClient;

    @Value("${kinopoisk.api-key}")
    private String apiKey;

    @Autowired
    public ApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> getResponse() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/random")
                        .build())
                .header("X-API-KEY", apiKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
    }
}
