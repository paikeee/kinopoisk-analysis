package ru.spbstu.kinopoisk_analysis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.spbstu.kinopoisk_analysis.exception.ForbiddenException;
import ru.spbstu.kinopoisk_analysis.exception.UnauthorizedException;

@Service
@RequiredArgsConstructor
public class ApiService {

    private final WebClient webClient;

    @Value("${kinopoisk.fetch.types}")
    private String[] fetchTypes;

    public Mono<String> getResponse(String apiKey) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/random")
                        .queryParam("rating.kp", "2-10")
                        .queryParam("type", fetchTypes)
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
