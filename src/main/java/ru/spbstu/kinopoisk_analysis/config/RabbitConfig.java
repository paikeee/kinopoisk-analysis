package ru.spbstu.kinopoisk_analysis.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.queue.name}")
    private String queue;

    @Bean
    public Queue getQueue() {
        return new Queue(queue, true);
    }
}
