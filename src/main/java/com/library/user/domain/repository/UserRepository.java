package com.library.user.domain.repository;

import com.library.user.domain.model.Email;
import com.library.user.domain.model.User;
import com.library.user.domain.model.UserId;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    User save(User user);
}
