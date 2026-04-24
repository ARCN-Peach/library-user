package com.library.user.application.usecase;

import com.library.user.application.exception.ConflictException;
import com.library.user.application.port.OutboxRepository;
import com.library.user.application.port.PasswordHasher;
import com.library.user.domain.event.DomainEvent;
import com.library.user.domain.event.UserRegisteredEvent;
import com.library.user.domain.model.Email;
import com.library.user.domain.model.User;
import com.library.user.domain.model.UserId;
import com.library.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUserUseCaseTest {

    private InMemoryUserRepository userRepository;
    private CapturingOutboxRepository outboxRepository;
    private PasswordHasher passwordHasher;
    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        outboxRepository = new CapturingOutboxRepository();
        passwordHasher = new PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                return "hashed-" + rawPassword;
            }

            @Override
            public boolean matches(String rawPassword, String hashedPassword) {
                return hashedPassword.equals("hashed-" + rawPassword);
            }
        };
        useCase = new RegisterUserUseCase(
                userRepository,
                passwordHasher,
                outboxRepository,
                Clock.fixed(Instant.parse("2026-04-23T00:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void execute_registers_reader_and_publishes_event() {
        var result = useCase.execute("Reader", "reader@test.com", "Password123", "corr-1");

        assertThat(result.email()).isEqualTo("reader@test.com");
        assertThat(userRepository.findByEmail(new Email("reader@test.com"))).isPresent();
        assertThat(outboxRepository.events).hasSize(1);
        assertThat(outboxRepository.events.getFirst()).isInstanceOf(UserRegisteredEvent.class);
    }

    @Test
    void execute_rejects_duplicate_email() {
        userRepository.save(User.registerReader(
                new com.library.user.domain.model.Name("Reader"),
                new Email("reader@test.com"),
                "hash",
                Instant.parse("2026-04-23T00:00:00Z")
        ));

        assertThatThrownBy(() -> useCase.execute("Reader", "reader@test.com", "Password123", "corr-1"))
                .isInstanceOf(ConflictException.class);
    }

    static class InMemoryUserRepository implements UserRepository {
        private final Map<UserId, User> users = new HashMap<>();

        @Override
        public Optional<User> findById(UserId id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public Optional<User> findByEmail(Email email) {
            return users.values().stream().filter(user -> user.email().equals(email)).findFirst();
        }

        @Override
        public boolean existsByEmail(Email email) {
            return findByEmail(email).isPresent();
        }

        @Override
        public User save(User user) {
            users.put(user.id(), user);
            return user;
        }
    }

    static class CapturingOutboxRepository implements OutboxRepository {
        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void save(DomainEvent event) {
            events.add(event);
        }

        @Override
        public List<OutboxMessage> findPending(int limit) {
            return List.of();
        }

        @Override
        public void markPublished(long id) {
        }

        @Override
        public void markFailed(long id, String error) {
        }

        List<DomainEvent> getEvents() {
            return events;
        }
    }
}
