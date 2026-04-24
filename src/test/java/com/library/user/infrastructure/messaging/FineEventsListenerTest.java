package com.library.user.infrastructure.messaging;

import static org.mockito.Mockito.verify;

import com.library.user.application.usecase.HandleFineGeneratedUseCase;
import com.library.user.application.usecase.HandleUserDebtClearedUseCase;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FineEventsListenerTest {

    @Test
    void onFineGeneratedUsesProvidedCorrelationId() {
        var handleFineGeneratedUseCase = Mockito.mock(HandleFineGeneratedUseCase.class);
        var handleUserDebtClearedUseCase = Mockito.mock(HandleUserDebtClearedUseCase.class);
        var listener = new FineEventsListener(handleFineGeneratedUseCase, handleUserDebtClearedUseCase);
        var userId = UUID.randomUUID();

        listener.onFineGenerated(new FineEventsListener.FineGeneratedMessage(UUID.randomUUID(), userId, "12.00"), "corr-1");

        verify(handleFineGeneratedUseCase).execute(userId, "fine generated", "corr-1");
    }

    @Test
    void onFineGeneratedFallsBackToSystemCorrelationId() {
        var handleFineGeneratedUseCase = Mockito.mock(HandleFineGeneratedUseCase.class);
        var handleUserDebtClearedUseCase = Mockito.mock(HandleUserDebtClearedUseCase.class);
        var listener = new FineEventsListener(handleFineGeneratedUseCase, handleUserDebtClearedUseCase);
        var userId = UUID.randomUUID();

        listener.onFineGenerated(new FineEventsListener.FineGeneratedMessage(UUID.randomUUID(), userId, "12.00"), null);

        verify(handleFineGeneratedUseCase).execute(userId, "fine generated", "system");
    }

    @Test
    void onUserDebtClearedDelegatesToUseCase() {
        var handleFineGeneratedUseCase = Mockito.mock(HandleFineGeneratedUseCase.class);
        var handleUserDebtClearedUseCase = Mockito.mock(HandleUserDebtClearedUseCase.class);
        var listener = new FineEventsListener(handleFineGeneratedUseCase, handleUserDebtClearedUseCase);
        var userId = UUID.randomUUID();

        listener.onUserDebtCleared(new FineEventsListener.UserDebtClearedMessage(userId));

        verify(handleUserDebtClearedUseCase).execute(userId);
    }
}
