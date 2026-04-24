package com.library.user.interfaces.http;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.library.user.application.dto.AuthTokens;
import com.library.user.application.dto.UserProfileView;
import com.library.user.application.port.TokenService;
import com.library.user.application.usecase.LoginUseCase;
import com.library.user.application.usecase.RefreshTokenUseCase;
import com.library.user.application.usecase.RegisterUserUseCase;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {AuthController.class, GlobalExceptionHandler.class, CorrelationIdFilter.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private LoginUseCase loginUseCase;

    @MockBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockBean
    private TokenService tokenService;

    @Test
    void registerReturnsCreated() throws Exception {
        var view = new UserProfileView(UUID.randomUUID(), "Reader", "reader@test.com", "READER", "ACTIVE", Instant.now(), Instant.now());
        when(registerUserUseCase.execute(eq("Reader"), eq("reader@test.com"), eq("Password123"), eq("corr-1")))
                .thenReturn(view);

        mockMvc.perform(post("/api/v1/auth/register")
                        .header("X-Correlation-Id", "corr-1")
                        .contentType("application/json")
                        .content("""
                                {"name":"Reader","email":"reader@test.com","password":"Password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("reader@test.com"));
    }

    @Test
    void loginReturnsTokens() throws Exception {
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
    void refreshReturnsTokens() throws Exception {
        when(refreshTokenUseCase.execute("refresh-token"))
                .thenReturn(new AuthTokens("access-2", "refresh-2"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("X-Correlation-Id", "corr-1")
                        .contentType("application/json")
                        .content("""
                                {"refreshToken":"refresh-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-2"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-2"));
    }
}
