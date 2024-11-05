package ru.spbstu.kinopoisk_analysis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.spbstu.kinopoisk_analysis.exception.ForbiddenException;
import ru.spbstu.kinopoisk_analysis.exception.UnauthorizedException;

@Service
public class ApiService {

    private final WebClient webClient;

    @Autowired
    public ApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> getResponse(String apiKey) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/random")
                        .queryParam("rating.kp", "2-10")
                        .queryParam("type", "movie", "cartoon", "tv-series")
                        .build())
                .header("X-API-KEY", apiKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        status -> status.value() == HttpStatus.FORBIDDEN.value(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new ForbiddenException(errorBody)))
                )
                .onStatus(
                        status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new UnauthorizedException(errorBody)))
                )
                .bodyToMono(String.class);
    }
}
