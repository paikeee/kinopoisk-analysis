package ru.spbstu.kinopoisk_analysis.client;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.spbstu.kinopoisk_analysis.service.ApiService;

@AllArgsConstructor
@Component
public class ApiClient {

    private final ApiService apiService;

    public void fetchJson() {
        Flux.range(1, 2)
                .flatMap(it -> apiService.getResponse())
                .subscribe(json -> System.out.println("JSON: " + json));
    }
}
