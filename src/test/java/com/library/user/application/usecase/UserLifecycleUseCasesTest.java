package com.library.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.library.user.application.port.PasswordHasher;
import com.library.user.domain.model.Email;
import com.library.user.domain.model.Name;
import com.library.user.domain.model.User;
import com.library.user.domain.model.UserStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserLifecycleUseCasesTest {

    private RegisterUserUseCaseTest.InMemoryUserRepository userRepository;
    private RegisterUserUseCaseTest.CapturingOutboxRepository outboxRepository;
    private ChangeUserStatusUseCase changeUserStatusUseCase;

    @BeforeEach
    void setUp() {
        userRepository = new RegisterUserUseCaseTest.InMemoryUserRepository();
        outboxRepository = new RegisterUserUseCaseTest.CapturingOutboxRepository();
        changeUserStatusUseCase = new ChangeUserStatusUseCase(
                userRepository,
                outboxRepository,
                Clock.fixed(Instant.parse("2026-04-23T00:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void unblockUserClearsBlockedStatusWithoutPublishingEvent() {
        var user = User.registerReader(new Name("Reader"), new Email("reader@test.com"), "hash", Instant.now());
        user.block(Instant.parse("2026-04-22T00:00:00Z"));
        userRepository.save(user);

        var result = changeUserStatusUseCase.execute(user.id().value(), false, "paid", "corr-2");

        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE.name());
        assertThat(outboxRepository.getEvents()).isEmpty();
    }

    @Test
    void blockingAlreadyBlockedUserDoesNotDuplicateEvent() {
        var user = User.registerReader(new Name("Reader"), new Email("reader@test.com"), "hash", Instant.now());
        user.block(Instant.parse("2026-04-22T00:00:00Z"));
        userRepository.save(user);

        changeUserStatusUseCase.execute(user.id().value(), true, "fine", "corr-3");

        assertThat(outboxRepository.getEvents()).isEmpty();
    }

    @Test
    void handleFineGeneratedDelegatesToBlockFlow() {
        var user = User.registerReader(new Name("Reader"), new Email("reader@test.com"), "hash", Instant.now());
        userRepository.save(user);
        var useCase = new HandleFineGeneratedUseCase(changeUserStatusUseCase);

        useCase.execute(user.id().value(), "fine generated", "corr-4");

        assertThat(userRepository.findById(user.id())).get().extracting(User::status).isEqualTo(UserStatus.BLOCKED);
        assertThat(outboxRepository.getEvents()).hasSize(1);
    }

    @Test
    void handleDebtClearedDelegatesToUnblockFlow() {
        var user = User.registerReader(new Name("Reader"), new Email("reader@test.com"), "hash", Instant.now());
        user.block(Instant.parse("2026-04-22T00:00:00Z"));
        userRepository.save(user);
        var useCase = new HandleUserDebtClearedUseCase(changeUserStatusUseCase);

        useCase.execute(user.id().value());

        assertThat(userRepository.findById(user.id())).get().extracting(User::status).isEqualTo(UserStatus.ACTIVE);
        assertThat(outboxRepository.getEvents()).isEmpty();
    }

    @Test
    void bootstrapLibrarianCreatesUserOnce() {
        var passwordHasher = new PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                return "hashed-" + rawPassword;
            }

            @Override
            public boolean matches(String rawPassword, String hashedPassword) {
                return hashedPassword.equals("hashed-" + rawPassword);
            }
        };
        var useCase = new BootstrapLibrarianUseCase(
                userRepository,
                passwordHasher,
                Clock.fixed(Instant.parse("2026-04-23T00:00:00Z"), ZoneOffset.UTC)
        );

        useCase.execute("Admin", "admin@test.com", "Password123");
        useCase.execute("Admin", "admin@test.com", "Password123");

        assertThat(userRepository.findByEmail(new Email("admin@test.com"))).isPresent();
        assertThat(userRepository.findByEmail(new Email("admin@test.com"))).get()
                .extracting(User::role)
                .asString()
                .isEqualTo("LIBRARIAN");
    }
}
