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
}
