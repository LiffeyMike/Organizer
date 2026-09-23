package com.organizer.adapter.persistence.user;

import org.springframework.stereotype.Component;

import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.User;

@Component
class UserMapper {

  User toDomain(JpaUser entity) {
    return new User(
        new UserId(entity.getId()),
        new Email(entity.getEmail()),
        new HashedPassword(entity.getPasswordHash()),
        entity.getDisplayName(),
        entity.getCreatedAt());
  }

  JpaUser toEntity(User user) {
    JpaUser entity = new JpaUser();

    entity.setId(user.id().id());
    entity.setEmail(user.email().value());
    entity.setPasswordHash(user.password().value());
    entity.setDisplayName(user.displayName());
    entity.setCreatedAt(user.createdAt());

    return entity;
  }
}
