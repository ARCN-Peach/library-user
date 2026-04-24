package com.library.user.application.usecase;

import com.library.user.application.dto.UserProfileView;
import com.library.user.application.exception.ConflictException;
import com.library.user.application.exception.NotFoundException;
import com.library.user.domain.model.Email;
import com.library.user.domain.model.Name;
import com.library.user.domain.model.UserId;
import com.library.user.domain.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateOwnProfileUseCase {

    private final UserRepository userRepository;
    private final Clock clock;

    public UpdateOwnProfileUseCase(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public UserProfileView execute(UUID userId, String name, String email) {
        var user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new NotFoundException("user not found"));
        var newEmail = new Email(email);
        if (!user.email().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new ConflictException("email already exists");
        }
        user.updateProfile(new Name(name), newEmail, Instant.now(clock));
        userRepository.save(user);
        return UserProfileView.from(user);
    }
}
