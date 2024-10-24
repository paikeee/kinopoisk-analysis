package ru.spbstu.kinopoisk_analysis.runner;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.spbstu.kinopoisk_analysis.client.ApiClient;

@AllArgsConstructor
@Component
public class ApiRunner implements CommandLineRunner {

    private final ApiClient apiClient;

    @Override
    public void run(String... args) {
        apiClient.fetchJson();
    }
}
