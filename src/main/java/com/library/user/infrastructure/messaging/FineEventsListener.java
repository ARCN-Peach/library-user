package com.library.user.infrastructure.messaging;

import com.library.user.application.usecase.HandleFineGeneratedUseCase;
import com.library.user.application.usecase.HandleUserDebtClearedUseCase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class FineEventsListener {

    private final HandleFineGeneratedUseCase handleFineGeneratedUseCase;
    private final HandleUserDebtClearedUseCase handleUserDebtClearedUseCase;

    public FineEventsListener(
            HandleFineGeneratedUseCase handleFineGeneratedUseCase,
            HandleUserDebtClearedUseCase handleUserDebtClearedUseCase
    ) {
        this.handleFineGeneratedUseCase = handleFineGeneratedUseCase;
        this.handleUserDebtClearedUseCase = handleUserDebtClearedUseCase;
    }

    @RabbitListener(queues = "library-user.fine-generated.v1")
    public void onFineGenerated(FineGeneratedMessage message, @Header(name = "correlationId", required = false) String correlationId) {
        handleFineGeneratedUseCase.execute(message.userId(), "fine generated", correlationId == null ? "system" : correlationId);
    }

    @RabbitListener(queues = "library-user.user-debt-cleared.v1")
    public void onUserDebtCleared(UserDebtClearedMessage message) {
        handleUserDebtClearedUseCase.execute(message.userId());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FineGeneratedMessage(UUID fineId, UUID userId, String amount) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserDebtClearedMessage(UUID userId) {}
}
