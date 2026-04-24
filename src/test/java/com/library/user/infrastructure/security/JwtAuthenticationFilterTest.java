package com.library.user.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.library.user.application.port.TokenService;
import com.library.user.domain.model.UserId;
import com.library.user.domain.model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsAuthenticationWhenAuthorizationHeaderIsMissing() throws Exception {
        var tokenService = Mockito.mock(TokenService.class);
        var filter = new JwtAuthenticationFilter(tokenService);
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = Mockito.mock(jakarta.servlet.FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void authenticatesBearerTokenIntoSecurityContext() throws Exception {
        var tokenService = Mockito.mock(TokenService.class);
        var filter = new JwtAuthenticationFilter(tokenService);
        var request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer access-token");
        var response = new MockHttpServletResponse();
        var chain = Mockito.mock(jakarta.servlet.FilterChain.class);
        var userId = UserId.newId();
        when(tokenService.parseAccessToken("access-token"))
                .thenReturn(new TokenService.AccessTokenPayload(userId, UserRole.LIBRARIAN));

        filter.doFilterInternal(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(new AuthenticatedUser(userId.value(), UserRole.LIBRARIAN));
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_LIBRARIAN");
        verify(chain).doFilter(request, response);
    }
}
