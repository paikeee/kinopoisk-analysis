package ru.spbstu.kinopoisk_analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.spbstu.kinopoisk_analysis.amqp.AmqpSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AmqpSenderTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private AmqpSender amqpSender;

    private static final String TEST_QUEUE_NAME = "test-queue";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        amqpSender = new AmqpSender(rabbitTemplate, TEST_QUEUE_NAME);
    }

    @Test
    void testSendMessage_Success() {
        String testMessage = "{\"key\": \"value\"}";

        amqpSender.sendMessage(testMessage);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(rabbitTemplate, times(1)).send(eq(TEST_QUEUE_NAME), messageCaptor.capture());

        Message capturedMessage = messageCaptor.getValue();
        assertEquals(testMessage, new String(capturedMessage.getBody()));
        assertEquals(MessageProperties.CONTENT_TYPE_JSON, capturedMessage.getMessageProperties().getContentType());
    }

    @Test
    void testSendMessage_EmptyMessage() {
        String emptyMessage = "";

        amqpSender.sendMessage(emptyMessage);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(rabbitTemplate, times(1)).send(eq(TEST_QUEUE_NAME), messageCaptor.capture());

        Message capturedMessage = messageCaptor.getValue();
        assertEquals(emptyMessage, new String(capturedMessage.getBody()));
        assertEquals(MessageProperties.CONTENT_TYPE_JSON, capturedMessage.getMessageProperties().getContentType());
    }
}
