package com.library.user.infrastructure.messaging;

import com.library.user.application.port.OutboxRepository;
import com.library.user.infrastructure.config.OutboxProperties;
import com.library.user.infrastructure.config.RabbitMqConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final OutboxProperties outboxProperties;

    public OutboxPublisher(OutboxRepository outboxRepository, RabbitTemplate rabbitTemplate, OutboxProperties outboxProperties) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.outboxProperties = outboxProperties;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-ms:5000}")
    public void publishPendingMessages() {
        for (var message : outboxRepository.findPending(outboxProperties.batchSize())) {
            try {
                Message amqpMessage = MessageBuilder
                        .withBody(message.payload().getBytes(StandardCharsets.UTF_8))
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .setCorrelationId(message.correlationId())
                        .setHeader("eventId", message.eventId())
                        .setHeader("eventType", message.eventType())
                        .build();

                rabbitTemplate.send(RabbitMqConfiguration.USER_EXCHANGE, message.eventType(), amqpMessage);
                outboxRepository.markPublished(message.id());
                log.info("Relayed event {} [id={}]", message.eventType(), message.id());
            } catch (Exception ex) {
                outboxRepository.markFailed(message.id(), ex.getMessage());
                log.error("Failed to relay outbox event {} [id={}]: {}",
                        message.eventType(), message.id(), ex.getMessage());
            }
        }
    }
}
