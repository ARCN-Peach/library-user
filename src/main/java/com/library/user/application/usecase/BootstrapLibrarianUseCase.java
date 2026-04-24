package com.library.user.application.usecase;

import com.library.user.application.port.PasswordHasher;
import com.library.user.domain.model.Email;
import com.library.user.domain.model.Name;
import com.library.user.domain.model.User;
import com.library.user.domain.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BootstrapLibrarianUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final Clock clock;

    public BootstrapLibrarianUseCase(UserRepository userRepository, PasswordHasher passwordHasher, Clock clock) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    @Transactional
    public void execute(String name, String email, String rawPassword) {
        var userEmail = new Email(email);
        if (userRepository.existsByEmail(userEmail)) {
            return;
        }
        var user = User.bootstrapLibrarian(new Name(name), userEmail, passwordHasher.hash(rawPassword), Instant.now(clock));
        userRepository.save(user);
    }
}
