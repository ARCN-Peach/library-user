package com.library.user.interfaces.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.library.user.application.dto.UserProfileView;
import com.library.user.application.usecase.ChangeUserStatusUseCase;
import com.library.user.application.usecase.GetUserProfileUseCase;
import com.library.user.application.usecase.UpdateOwnProfileUseCase;
import com.library.user.domain.model.UserRole;
import com.library.user.infrastructure.security.AuthenticatedUser;
import com.library.user.interfaces.http.request.ChangeUserStatusRequest;
import com.library.user.interfaces.http.request.UpdateUserProfileRequest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserControllerTest {

    private final GetUserProfileUseCase getUserProfileUseCase = Mockito.mock(GetUserProfileUseCase.class);
    private final UpdateOwnProfileUseCase updateOwnProfileUseCase = Mockito.mock(UpdateOwnProfileUseCase.class);
    private final ChangeUserStatusUseCase changeUserStatusUseCase = Mockito.mock(ChangeUserStatusUseCase.class);

    private final UserController controller = new UserController(
            getUserProfileUseCase,
            updateOwnProfileUseCase,
            changeUserStatusUseCase
    );

    @Test
    void meReturnsCurrentUserProfile() {
        var userId = UUID.randomUUID();
        when(getUserProfileUseCase.execute(userId)).thenReturn(profile(userId, "Reader", "reader@test.com", "READER", "ACTIVE"));

        var response = controller.me(new AuthenticatedUser(userId, UserRole.READER));

        assertThat(response.email()).isEqualTo("reader@test.com");
    }

    @Test
    void updateMeReturnsUpdatedProfile() {
        var userId = UUID.randomUUID();
        when(updateOwnProfileUseCase.execute(userId, "Reader Two", "reader2@test.com"))
                .thenReturn(profile(userId, "Reader Two", "reader2@test.com", "READER", "ACTIVE"));

        var response = controller.updateMe(
                new AuthenticatedUser(userId, UserRole.READER),
                new UpdateUserProfileRequest("Reader Two", "reader2@test.com")
        );

        assertThat(response.name()).isEqualTo("Reader Two");
    }

    @Test
    void getByIdReturnsProfile() {
        var userId = UUID.randomUUID();
        when(getUserProfileUseCase.execute(userId)).thenReturn(profile(userId, "Reader", "reader@test.com", "READER", "ACTIVE"));

        var response = controller.getById(userId);

        assertThat(response.userId()).isEqualTo(userId);
    }

    @Test
    void changeStatusReturnsUpdatedProfile() {
        var userId = UUID.randomUUID();
        when(changeUserStatusUseCase.execute(userId, true, "fine", "corr-1"))
                .thenReturn(profile(userId, "Reader", "reader@test.com", "READER", "BLOCKED"));

        var response = controller.changeStatus(userId, new ChangeUserStatusRequest(true, "fine"), "corr-1");

        assertThat(response.status()).isEqualTo("BLOCKED");
    }

    private UserProfileView profile(UUID userId, String name, String email, String role, String status) {
        var now = Instant.parse("2026-04-23T00:00:00Z");
        return new UserProfileView(userId, name, email, role, status, now, now);
    }
}
