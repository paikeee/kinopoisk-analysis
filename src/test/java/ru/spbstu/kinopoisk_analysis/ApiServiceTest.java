package ru.spbstu.kinopoisk_analysis;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.spbstu.kinopoisk_analysis.exception.BadRequest;
import ru.spbstu.kinopoisk_analysis.exception.ForbiddenException;
import ru.spbstu.kinopoisk_analysis.exception.UnauthorizedException;
import ru.spbstu.kinopoisk_analysis.service.ApiService;

import java.io.IOException;

class ApiServiceTest {

    private MockWebServer mockWebServer;
    private ApiService fetcherService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        fetcherService = new ApiService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetResponse_Success() {
        String expectedResponse = "{\"status\":\"ok\"}";

        mockWebServer.enqueue(new MockResponse()
                .setBody(expectedResponse)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(HttpStatus.OK.value()));

        Mono<String> response = fetcherService.getResponse("validApiKey", 1);

        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void testGetResponse_Forbidden() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("Access Denied")
                .setResponseCode(HttpStatus.FORBIDDEN.value()));

        Mono<String> response = fetcherService.getResponse("invalidApiKey", 1);

        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof ForbiddenException &&
                        throwable.getMessage().equals("Access Denied"))
                .verify();
    }

    @Test
    void testGetResponse_Unauthorized() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("Unauthorized")
                .setResponseCode(HttpStatus.UNAUTHORIZED.value()));

        Mono<String> response = fetcherService.getResponse("wrongApiKey", 1);

        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof UnauthorizedException &&
                        throwable.getMessage().equals("Unauthorized"))
                .verify();
    }

    @Test
    void testGetResponse_BadRequest() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("Bad Request")
                .setResponseCode(HttpStatus.BAD_REQUEST.value()));

        Mono<String> response = fetcherService.getResponse("validApiKey", -1);

        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof BadRequest &&
                        throwable.getMessage().equals("Bad Request"))
                .verify();
    }
}