package ru.spbstu.kinopoisk_analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import ru.spbstu.kinopoisk_analysis.amqp.AmqpSender;
import ru.spbstu.kinopoisk_analysis.client.ApiClient;
import ru.spbstu.kinopoisk_analysis.exception.BadRequest;
import ru.spbstu.kinopoisk_analysis.exception.ForbiddenException;
import ru.spbstu.kinopoisk_analysis.exception.UnauthorizedException;
import ru.spbstu.kinopoisk_analysis.service.ApiService;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiClientTest {

    @Mock
    private AmqpSender amqpSender;

    @Mock
    private ApiService apiService;

    private ApiClient apiClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        String testApiKeys = "test-key1;test-key2";
        apiClient = new ApiClient(amqpSender, apiService, testApiKeys);
    }

    @Test
    void testFetchJson_Success() {
        when(apiService.getResponse("test-key1", 1)).thenReturn(Mono.just("response1"));
        when(apiService.getResponse("test-key2", 2)).thenReturn(Mono.just("response2"));

        apiClient.fetchJson();

        verify(amqpSender, times(1)).sendMessage("response1");
        verify(amqpSender, times(1)).sendMessage("response2");
    }

    @Test
    void testFetchJson_ForbiddenError() {
        when(apiService.getResponse("test-key1", 1))
                .thenReturn(Mono.error(new ForbiddenException("Forbidden")));
        when(apiService.getResponse("test-key2", 2)).thenReturn(Mono.just("response2"));

        apiClient.fetchJson();

        verify(amqpSender, times(1)).sendMessage("response2");
        verify(apiService, times(1)).getResponse("test-key1", 1);
        verify(apiService, times(1)).getResponse("test-key2", 2);
    }

    @Test
    void testFetchJson_UnauthorizedError() {
        when(apiService.getResponse("test-key1", 1))
                .thenReturn(Mono.error(new UnauthorizedException("Unauthorized")));
        when(apiService.getResponse("test-key2", 2)).thenReturn(Mono.just("response2"));

        apiClient.fetchJson();

        verify(amqpSender, times(1)).sendMessage("response2");
        verify(apiService, times(1)).getResponse("test-key1", 1);
        verify(apiService, times(1)).getResponse("test-key2", 2);
    }

    @Test
    void testFetchJson_BadRequest() {
        when(apiService.getResponse("test-key1", 1))
                .thenReturn(Mono.error(new BadRequest("Bad request")));
        when(apiService.getResponse("test-key2", 2)).thenReturn(Mono.just("response2"));

        apiClient.fetchJson();

        verify(amqpSender, times(1)).sendMessage("response2");
        verify(apiService, times(1)).getResponse("test-key1", 1);
        verify(apiService, times(1)).getResponse("test-key2", 2);
    }

    @Test
    void testMaskApi() {
        String originalKey = "test-key-ABCDEFG";
        String maskedKey = ApiClient.maskApi(originalKey);

        assert(maskedKey.equals("test-key-*******"));
    }
}
