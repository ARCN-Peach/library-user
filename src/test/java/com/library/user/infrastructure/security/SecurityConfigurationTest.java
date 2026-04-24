package com.library.user.infrastructure.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.library.user.application.dto.AuthTokens;
import com.library.user.application.dto.UserProfileView;
import com.library.user.application.port.TokenService;
import com.library.user.application.usecase.ChangeUserStatusUseCase;
import com.library.user.application.usecase.GetUserProfileUseCase;
import com.library.user.application.usecase.LoginUseCase;
import com.library.user.application.usecase.RefreshTokenUseCase;
import com.library.user.application.usecase.RegisterUserUseCase;
import com.library.user.application.usecase.UpdateOwnProfileUseCase;
import com.library.user.domain.model.UserId;
import com.library.user.domain.model.UserRole;
import com.library.user.interfaces.http.AuthController;
import com.library.user.interfaces.http.CorrelationIdFilter;
import com.library.user.interfaces.http.GlobalExceptionHandler;
import com.library.user.interfaces.http.UserController;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        AuthController.class,
        UserController.class,
        GlobalExceptionHandler.class,
        CorrelationIdFilter.class
})
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class})
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private LoginUseCase loginUseCase;

    @MockBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockBean
    private GetUserProfileUseCase getUserProfileUseCase;

    @MockBean
    private UpdateOwnProfileUseCase updateOwnProfileUseCase;

    @MockBean
    private ChangeUserStatusUseCase changeUserStatusUseCase;

    @MockBean
    private TokenService tokenService;

    @Test
    void loginEndpointIsPublic() throws Exception {
        when(loginUseCase.execute("reader@test.com", "Password123"))
                .thenReturn(new AuthTokens("access", "refresh"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Correlation-Id", "corr-1")
                        .contentType("application/json")
                        .content("""
                                {"email":"reader@test.com","password":"Password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void protectedEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-Correlation-Id", "corr-1"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void bearerTokenAuthenticatesProtectedEndpoint() throws Exception {
        var userId = UUID.randomUUID();
        when(tokenService.parseAccessToken("access-token"))
                .thenReturn(new TokenService.AccessTokenPayload(new UserId(userId), UserRole.READER));
        when(getUserProfileUseCase.execute(userId))
                .thenReturn(new UserProfileView(
                        userId,
                        "Reader",
                        "reader@test.com",
                        "READER",
                        "ACTIVE",
                        Instant.parse("2026-04-23T00:00:00Z"),
                        Instant.parse("2026-04-23T00:00:00Z")
                ));

        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-Correlation-Id", "corr-1")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("reader@test.com"));
    }
}
