package com.library.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void blockedUserCannotAuthenticate() {
        var user = User.registerReader(new Name("Reader"), new Email("reader@test.com"), "hash", Instant.now());
        user.block(Instant.now());

        assertThatThrownBy(user::assertCanAuthenticate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocked");
    }

    @Test
    void updateProfileChangesNameAndEmail() {
        var user = User.registerReader(new Name("Reader"), new Email("reader@test.com"), "hash", Instant.now());
        var now = Instant.now();

        user.updateProfile(new Name("Reader Two"), new Email("reader2@test.com"), now);

        assertThat(user.name().value()).isEqualTo("Reader Two");
        assertThat(user.email().value()).isEqualTo("reader2@test.com");
        assertThat(user.updatedAt()).isEqualTo(now);
    }

    @Test
    void librarianCannotChangeEmail() {
        var user = User.bootstrapLibrarian(new Name("Librarian"), new Email("lib@test.com"), "hash", Instant.now());

        assertThatThrownBy(() -> user.updateProfile(new Name("Admin"), new Email("other@test.com"), Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("librarian email cannot be changed");
    }

    @Test
    void blockReturnsFalseWhenAlreadyBlocked() {
        var user = User.registerReader(new Name("Reader"), new Email("reader@test.com"), "hash", Instant.now());
        user.block(Instant.now());

        assertThat(user.block(Instant.now())).isFalse();
    }

    @Test
    void unblockReturnsFalseWhenAlreadyActive() {
        var user = User.registerReader(new Name("Reader"), new Email("reader@test.com"), "hash", Instant.now());

        assertThat(user.unblock(Instant.now())).isFalse();
    }

    @Test
    void bootstrapLibrarianCreatesLibrarianUser() {
        var user = User.bootstrapLibrarian(new Name("Librarian"), new Email("lib@test.com"), "hash", Instant.now());

        assertThat(user.role()).isEqualTo(UserRole.LIBRARIAN);
        assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);
    }
}
