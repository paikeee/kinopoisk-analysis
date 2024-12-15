package ru.spbstu.kinopoisk_analysis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import ru.spbstu.kinopoisk_analysis.exception.BadRequest;
import ru.spbstu.kinopoisk_analysis.exception.ForbiddenException;
import ru.spbstu.kinopoisk_analysis.exception.UnauthorizedException;

@Service
public class ApiService {

    private static final Object[] FIELDS = {
            "id", "externalId", "name", "enName", "alternativeName", "names", "description", "shortDescription",
            "slogan", "type", "typeNumber", "isSeries", "status", "year", "releaseYears", "rating", "ratingMpaa",
            "ageRating", "votes", "seasonsInfo", "budget", "audience", "movieLength", "seriesLength", "totalSeriesLength",
            "genres", "countries", "poster", "backdrop", "logo", "ticketsOnSale", "videos", "networks", "persons", "facts",
            "fees", "premiere", "similarMovies", "sequelsAndPrequels", "watchability", "lists", "top10", "top250", "updatedAt",
            "createdAt"
    };

    private final WebClient webClient;

    @Value("${kinopoisk.fetch.types}")
    private String[] fetchTypes;

    @Autowired
    public ApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> getResponse(String apiKey, Integer pageNumber) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie")
                        .queryParam("sortField", "id")
                        .queryParam("sortType", "1")
                        .queryParam("selectFields", FIELDS)
                        .queryParam("rating.kp", "2-10")
                        .queryParam("type", fetchTypes)
                        .queryParam("limit", "100")
                        .queryParam("page", pageNumber.toString())
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
                .onStatus(status -> status.value() == HttpStatus.BAD_REQUEST.value(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new BadRequest(errorBody)))
                        )
                .bodyToMono(String.class);
    }
}
