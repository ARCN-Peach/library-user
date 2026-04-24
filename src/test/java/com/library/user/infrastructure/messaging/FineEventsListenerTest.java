package com.library.user.infrastructure.messaging;

import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.user.application.usecase.HandleFineGeneratedUseCase;
import com.library.user.application.usecase.HandleFinePaidUseCase;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FineEventsListenerTest {

    @Test
    void onFineGeneratedUsesProvidedCorrelationId() {
        var handleFineGeneratedUseCase = Mockito.mock(HandleFineGeneratedUseCase.class);
        var handleFinePaidUseCase = Mockito.mock(HandleFinePaidUseCase.class);
        var listener = new FineEventsListener(new ObjectMapper(), handleFineGeneratedUseCase, handleFinePaidUseCase);
        var userId = UUID.randomUUID();
        var correlationId = UUID.randomUUID();

        org.assertj.core.api.Assertions.assertThatCode(() -> listener.onFineGenerated("""
                {"correlationId":"%s","fineId":"%s","userId":"%s","amount":"12.00"}
                """.formatted(correlationId, UUID.randomUUID(), userId)))
                .doesNotThrowAnyException();

        verify(handleFineGeneratedUseCase).execute(userId, "fine generated", correlationId.toString());
    }

    @Test
    void onFineGeneratedFallsBackToSystemCorrelationId() {
        var handleFineGeneratedUseCase = Mockito.mock(HandleFineGeneratedUseCase.class);
        var handleFinePaidUseCase = Mockito.mock(HandleFinePaidUseCase.class);
        var listener = new FineEventsListener(new ObjectMapper(), handleFineGeneratedUseCase, handleFinePaidUseCase);
        var userId = UUID.randomUUID();

        org.assertj.core.api.Assertions.assertThatCode(() -> listener.onFineGenerated("""
                {"fineId":"%s","userId":"%s","amount":"12.00"}
                """.formatted(UUID.randomUUID(), userId)))
                .doesNotThrowAnyException();

        verify(handleFineGeneratedUseCase).execute(userId, "fine generated", "system");
    }

    @Test
    void onFinePaidDelegatesToUseCase() {
        var handleFineGeneratedUseCase = Mockito.mock(HandleFineGeneratedUseCase.class);
        var handleFinePaidUseCase = Mockito.mock(HandleFinePaidUseCase.class);
        var listener = new FineEventsListener(new ObjectMapper(), handleFineGeneratedUseCase, handleFinePaidUseCase);
        var userId = UUID.randomUUID();

        org.assertj.core.api.Assertions.assertThatCode(() -> listener.onFinePaid("""
                {"fineId":"%s","userId":"%s"}
                """.formatted(UUID.randomUUID(), userId)))
                .doesNotThrowAnyException();

        verify(handleFinePaidUseCase).execute(userId);
    }
}
