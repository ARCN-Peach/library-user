package com.library.user.infrastructure.persistence.adapter;

import com.library.user.domain.model.Email;
import com.library.user.domain.model.User;
import com.library.user.domain.model.UserId;
import com.library.user.domain.repository.UserRepository;
import com.library.user.infrastructure.persistence.mapper.UserMapper;
import com.library.user.infrastructure.persistence.repository.SpringDataUserJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresUserRepository implements UserRepository {

    private final SpringDataUserJpaRepository repository;
    private final UserMapper mapper;

    public PostgresUserRepository(SpringDataUserJpaRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findById(UserId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return repository.findByEmail(email.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return repository.existsByEmail(email.value());
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }
}
