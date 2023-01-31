package com.example.javakinesisconsumer;

import java.util.function.Consumer;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

@Log4j2
@Configuration
public class KinesisConsumer {

    public KinesisConsumer() {

    }

    @Bean
    Consumer<Message<String>> myKinesisEvent() {
        return message -> {
            try {
                log.info("Event Payload: {}", message.getPayload());
            } catch (Exception e) {
                log.error("Kinesis consumer error", e);
            }
        };
    }
}
