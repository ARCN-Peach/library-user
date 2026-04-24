package com.library.user.application.usecase;

import com.library.user.application.exception.ConflictException;
import com.library.user.application.exception.NotFoundException;
import com.library.user.domain.model.Email;
import com.library.user.domain.model.User;
import com.library.user.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserProfileUseCasesTest {

    private RegisterUserUseCaseTest.InMemoryUserRepository userRepository;
    private GetUserProfileUseCase getUserProfileUseCase;
    private UpdateOwnProfileUseCase updateOwnProfileUseCase;
    private ChangeUserStatusUseCase changeUserStatusUseCase;
    private RegisterUserUseCaseTest.CapturingOutboxRepository outboxRepository;

    @BeforeEach
    void setUp() {
        userRepository = new RegisterUserUseCaseTest.InMemoryUserRepository();
        outboxRepository = new RegisterUserUseCaseTest.CapturingOutboxRepository();
        getUserProfileUseCase = new GetUserProfileUseCase(userRepository);
        updateOwnProfileUseCase = new UpdateOwnProfileUseCase(
                userRepository,
                Clock.fixed(Instant.parse("2026-04-23T00:00:00Z"), ZoneOffset.UTC)
        );
        changeUserStatusUseCase = new ChangeUserStatusUseCase(
                userRepository,
                outboxRepository,
                Clock.fixed(Instant.parse("2026-04-23T00:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void getUserProfile_returns_profile() {
        var user = seedUser("reader@test.com");

        var result = getUserProfileUseCase.execute(user.id().value());

        assertThat(result.email()).isEqualTo("reader@test.com");
    }

    @Test
    void getUserProfile_fails_when_missing() {
        assertThatThrownBy(() -> getUserProfileUseCase.execute(java.util.UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateOwnProfile_updates_name_and_email() {
        var user = seedUser("reader@test.com");

        var result = updateOwnProfileUseCase.execute(user.id().value(), "Reader Two", "reader2@test.com");

        assertThat(result.name()).isEqualTo("Reader Two");
        assertThat(result.email()).isEqualTo("reader2@test.com");
    }

    @Test
    void updateOwnProfile_rejects_duplicate_email() {
        var user = seedUser("reader@test.com");
        seedUser("other@test.com");

        assertThatThrownBy(() -> updateOwnProfileUseCase.execute(user.id().value(), "Reader", "other@test.com"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void changeUserStatus_blocks_user_and_publishes_event() {
        var user = seedUser("reader@test.com");

        var result = changeUserStatusUseCase.execute(user.id().value(), true, "fine", "corr-1");

        assertThat(result.status()).isEqualTo(UserStatus.BLOCKED.name());
        assertThat(outboxRepository.getEvents()).hasSize(1);
    }

    private User seedUser(String email) {
        var user = User.registerReader(
                new com.library.user.domain.model.Name("Reader"),
                new Email(email),
                "hash",
                Instant.parse("2026-04-23T00:00:00Z")
        );
        userRepository.save(user);
        return user;
    }
}
