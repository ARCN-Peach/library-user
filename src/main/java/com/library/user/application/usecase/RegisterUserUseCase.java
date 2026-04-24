package com.library.user.application.usecase;

import com.library.user.application.dto.UserProfileView;
import com.library.user.application.exception.ConflictException;
import com.library.user.application.port.OutboxRepository;
import com.library.user.application.port.PasswordHasher;
import com.library.user.domain.event.UserRegisteredEvent;
import com.library.user.domain.model.Email;
import com.library.user.domain.model.Name;
import com.library.user.domain.model.User;
import com.library.user.domain.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final OutboxRepository outboxRepository;
    private final Clock clock;

    public RegisterUserUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            OutboxRepository outboxRepository,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.outboxRepository = outboxRepository;
        this.clock = clock;
    }

    @Transactional
    public UserProfileView execute(String name, String email, String rawPassword, String correlationId) {
        var userName = new Name(name);
        var userEmail = new Email(email);
        if (userRepository.existsByEmail(userEmail)) {
            throw new ConflictException("email already exists");
        }

        var now = Instant.now(clock);
        var user = User.registerReader(userName, userEmail, passwordHasher.hash(rawPassword), now);
        userRepository.save(user);
        outboxRepository.save(UserRegisteredEvent.from(user, now, correlationId));
        return UserProfileView.from(user);
    }
}
