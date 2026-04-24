package com.library.user.infrastructure.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.library.user.application.port.OutboxRepository;
import com.library.user.infrastructure.config.OutboxProperties;
import com.library.user.infrastructure.config.RabbitMqConfiguration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class OutboxPublisherTest {

    @Test
    void publishesPendingMessagesAndMarksThemPublished() {
        var repository = Mockito.mock(OutboxRepository.class);
        var rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        var properties = new OutboxProperties(10, RabbitMqConfiguration.USER_EXCHANGE);
        var publisher = new OutboxPublisher(repository, rabbitTemplate, properties);
        var message = new OutboxRepository.OutboxMessage(1L, "evt-1", "user.blocked.v1", "agg-1", "corr-1", "{\"id\":1}");
        when(repository.findPending(10)).thenReturn(List.of(message));

        publisher.publishPendingMessages();

        verify(rabbitTemplate).send(eq(RabbitMqConfiguration.USER_EXCHANGE), eq("user.blocked.v1"), any(Message.class));
        verify(repository).markPublished(1L);
    }

    @Test
    void marksMessageAsFailedWhenPublishThrows() {
        var repository = Mockito.mock(OutboxRepository.class);
        var rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        var properties = new OutboxProperties(10, RabbitMqConfiguration.USER_EXCHANGE);
        var publisher = new OutboxPublisher(repository, rabbitTemplate, properties);
        var message = new OutboxRepository.OutboxMessage(2L, "evt-2", "user.registered.v1", "agg-2", "corr-2", "{\"id\":2}");
        when(repository.findPending(10)).thenReturn(List.of(message));
        doThrow(new IllegalStateException("broker down")).when(rabbitTemplate)
                .send(eq(RabbitMqConfiguration.USER_EXCHANGE), eq("user.registered.v1"), any(Message.class));

        publisher.publishPendingMessages();

        verify(repository).markFailed(2L, "broker down");
    }
}
