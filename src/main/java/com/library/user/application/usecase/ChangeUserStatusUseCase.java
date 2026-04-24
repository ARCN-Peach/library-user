package com.library.user.application.usecase;

import com.library.user.application.dto.UserProfileView;
import com.library.user.application.exception.NotFoundException;
import com.library.user.application.port.OutboxRepository;
import com.library.user.domain.event.UserBlockedEvent;
import com.library.user.domain.model.UserId;
import com.library.user.domain.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeUserStatusUseCase {

    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final Clock clock;

    public ChangeUserStatusUseCase(UserRepository userRepository, OutboxRepository outboxRepository, Clock clock) {
        this.userRepository = userRepository;
        this.outboxRepository = outboxRepository;
        this.clock = clock;
    }

    @Transactional
    public UserProfileView execute(UUID userId, boolean blocked, String reason, String correlationId) {
        var now = Instant.now(clock);
        var user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new NotFoundException("user not found"));
        var changed = blocked ? user.block(now) : user.unblock(now);
        userRepository.save(user);
        if (blocked && changed) {
            outboxRepository.save(UserBlockedEvent.from(user, now, correlationId, reason));
        }
        return UserProfileView.from(user);
    }
}
