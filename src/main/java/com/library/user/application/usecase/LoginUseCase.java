package com.library.user.application.usecase;

import com.library.user.application.dto.AuthTokens;
import com.library.user.application.exception.UnauthorizedException;
import com.library.user.application.port.PasswordHasher;
import com.library.user.domain.model.Email;
import com.library.user.domain.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AuthTokenIssuer authTokenIssuer;
    private final Clock clock;

    public LoginUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AuthTokenIssuer authTokenIssuer,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.authTokenIssuer = authTokenIssuer;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AuthTokens execute(String email, String rawPassword) {
        var user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new UnauthorizedException("invalid credentials"));
        if (!passwordHasher.matches(rawPassword, user.passwordHash())) {
            throw new UnauthorizedException("invalid credentials");
        }
        user.assertCanAuthenticate();
        return authTokenIssuer.issue(user, Instant.now(clock));
    }
}
