package com.library.user.infrastructure.persistence.mapper;

import com.library.user.domain.model.Email;
import com.library.user.domain.model.Name;
import com.library.user.domain.model.User;
import com.library.user.domain.model.UserId;
import com.library.user.domain.model.UserRole;
import com.library.user.domain.model.UserStatus;
import com.library.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        return User.rehydrate(
                new UserId(entity.getId()),
                new Name(entity.getName()),
                new Email(entity.getEmail()),
                entity.getPasswordHash(),
                UserRole.valueOf(entity.getRole()),
                UserStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public UserEntity toEntity(User user) {
        var entity = new UserEntity();
        entity.setId(user.id().value());
        entity.setName(user.name().value());
        entity.setEmail(user.email().value());
        entity.setPasswordHash(user.passwordHash());
        entity.setRole(user.role().name());
        entity.setStatus(user.status().name());
        entity.setCreatedAt(user.createdAt());
        entity.setUpdatedAt(user.updatedAt());
        return entity;
    }
}
