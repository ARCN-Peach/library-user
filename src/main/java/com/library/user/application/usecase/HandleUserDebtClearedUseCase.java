package com.library.user.application.usecase;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HandleUserDebtClearedUseCase {

    private final ChangeUserStatusUseCase changeUserStatusUseCase;

    public HandleUserDebtClearedUseCase(ChangeUserStatusUseCase changeUserStatusUseCase) {
        this.changeUserStatusUseCase = changeUserStatusUseCase;
    }

    public void execute(UUID userId) {
        changeUserStatusUseCase.execute(userId, false, "debt cleared", "system");
    }
}
