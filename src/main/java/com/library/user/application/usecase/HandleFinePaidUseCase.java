package com.library.user.application.usecase;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class HandleFinePaidUseCase {

    private final ChangeUserStatusUseCase changeUserStatusUseCase;

    public HandleFinePaidUseCase(ChangeUserStatusUseCase changeUserStatusUseCase) {
        this.changeUserStatusUseCase = changeUserStatusUseCase;
    }

    public void execute(UUID userId) {
        changeUserStatusUseCase.execute(userId, false, "fine paid", "system");
    }
}
