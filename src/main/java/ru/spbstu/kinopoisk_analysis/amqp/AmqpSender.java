package ru.spbstu.kinopoisk_analysis.amqp;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AmqpSender {

    private final RabbitTemplate rabbitTemplate;
    private final String queue;

    @Autowired
    public AmqpSender(RabbitTemplate rabbitTemplate, @Value("${rabbitmq.queue.name}") String queue) {
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;
    }

    public void sendMessage(String str) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);

        Message message = new Message(str.getBytes(), properties);
        rabbitTemplate.send(queue, message);
    }
}
