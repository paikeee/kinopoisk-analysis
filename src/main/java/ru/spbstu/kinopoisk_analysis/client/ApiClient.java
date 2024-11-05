package ru.spbstu.kinopoisk_analysis.client;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.spbstu.kinopoisk_analysis.amqp.AmqpSender;
import ru.spbstu.kinopoisk_analysis.exception.ForbiddenException;
import ru.spbstu.kinopoisk_analysis.exception.UnauthorizedException;
import ru.spbstu.kinopoisk_analysis.service.ApiService;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
@Component
@EnableScheduling
public class ApiClient {

    private final AmqpSender amqpSender;
    private final ApiService apiService;
    private final ApiKey[] apiKeys;

    private final AtomicBoolean lastResponseWasEmpty = new AtomicBoolean(false);

    @Autowired
    public ApiClient(AmqpSender amqpSender, ApiService apiService, @Value("${kinopoisk.api-key}") String api) {
        this.amqpSender = amqpSender;
        this.apiService = apiService;
        this.apiKeys = Arrays.stream(api.split(";")).map(ApiKey::new).toArray(ApiKey[]::new);
    }

    @Scheduled(cron = "${download.time.cron}")
    public void fetchJson() {
        log.info("Started JSON fetcher...");
        lastResponseWasEmpty.set(false);

        Flux.defer(() ->
                    Flux.range(1, apiKeys.length)
                    .flatMap(i -> apiService.getResponse(apiKeys[i - 1].getValue())
                            .onErrorResume(ForbiddenException.class,
                                    error -> handleApiError(apiKeys[i - 1], "Reached the limit for key: "))
                            .onErrorResume(UnauthorizedException.class,
                                    error -> handleApiError(apiKeys[i - 1], "Key is not available: "))
                    )
                    .filter(it -> !it.isEmpty())
                    .collectList()
                    .flatMapMany(list -> {
                        lastResponseWasEmpty.set(list.isEmpty());
                        if (list.isEmpty()) {
                            log.info("Reached day limit. No more data to process, stopping.");
                            return Flux.empty();
                        }
                        return Flux.fromIterable(list);
                    })
                )
                .repeat(() -> !lastResponseWasEmpty.get())
                .subscribe(
                        amqpSender::sendMessage,
                        error -> log.error("Error while sending json to the queue.")
                );
    }

    private Mono<String> handleApiError(ApiKey key, String message) {
        if (key.isToLog()) {
            log.warn(maskApi(key.getValue()) + " " + message);
            key.setToLog(false);
        }
        return Mono.empty();
    }

    private static String maskApi(String s) {
        return s.replaceAll("(?<=-)[A-Z0-9]{7}", "*******");
    }

    @Getter
    private static class ApiKey {

        private final String value;
        private boolean toLog;

        public ApiKey(String value) {
            this.value = value;
            this.toLog = true;
        }

        public void setToLog(boolean toLog) {
            this.toLog = toLog;
        }
    }
}
