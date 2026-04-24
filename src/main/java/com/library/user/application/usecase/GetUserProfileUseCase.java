package com.library.user.application.usecase;

import com.library.user.application.dto.UserProfileView;
import com.library.user.application.exception.NotFoundException;
import com.library.user.domain.model.UserId;
import com.library.user.domain.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetUserProfileUseCase {

    private final UserRepository userRepository;

    public GetUserProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileView execute(UUID userId) {
        return userRepository.findById(new UserId(userId))
                .map(UserProfileView::from)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }
}
