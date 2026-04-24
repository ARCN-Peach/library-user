package com.library.user.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.user.application.usecase.HandleFineGeneratedUseCase;
import com.library.user.application.usecase.HandleFinePaidUseCase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class FineEventsListener {

    private final ObjectMapper objectMapper;
    private final HandleFineGeneratedUseCase handleFineGeneratedUseCase;
    private final HandleFinePaidUseCase handleFinePaidUseCase;

    public FineEventsListener(
            ObjectMapper objectMapper,
            HandleFineGeneratedUseCase handleFineGeneratedUseCase,
            HandleFinePaidUseCase handleFinePaidUseCase
    ) {
        this.objectMapper = objectMapper;
        this.handleFineGeneratedUseCase = handleFineGeneratedUseCase;
        this.handleFinePaidUseCase = handleFinePaidUseCase;
    }

    @RabbitListener(queues = "user.fine-generated")
    public void onFineGenerated(String payload) throws Exception {
        var message = objectMapper.readValue(payload, FineGeneratedMessage.class);
        var correlationId = message.correlationId() == null ? "system" : message.correlationId().toString();
        handleFineGeneratedUseCase.execute(message.userId(), "fine generated", correlationId);
    }

    @RabbitListener(queues = "user.fine-paid")
    public void onFinePaid(String payload) throws Exception {
        var message = objectMapper.readValue(payload, FinePaidMessage.class);
        handleFinePaidUseCase.execute(message.userId());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FineGeneratedMessage(UUID correlationId, UUID fineId, UUID userId, String amount) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FinePaidMessage(UUID correlationId, UUID fineId, UUID userId) {}
}
