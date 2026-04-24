package com.library.user.application.usecase;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HandleFineGeneratedUseCase {

    private final ChangeUserStatusUseCase changeUserStatusUseCase;

    public HandleFineGeneratedUseCase(ChangeUserStatusUseCase changeUserStatusUseCase) {
        this.changeUserStatusUseCase = changeUserStatusUseCase;
    }

    public void execute(UUID userId, String reason, String correlationId) {
        changeUserStatusUseCase.execute(userId, true, reason, correlationId);
    }
}
