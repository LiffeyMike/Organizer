package com.organizer.adapter.persistence.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.User;

@Mapper(componentModel = "spring")
interface UserMapper {

  @Mapping(target = "id", expression = "java(new UserId(entity.getId()))")
  @Mapping(target = "email", expression = "java(new Email(entity.getEmail()))")
  @Mapping(target = "password", expression = "java(new HashedPassword(entity.getPasswordHash()))")
  User toDomain(JpaUser entity);

  default JpaUser toEntity(User user) {
    JpaUser entity = new JpaUser();

    entity.setId(user.id().id());
    entity.setEmail(user.email().value());
    entity.setPasswordHash(user.password().value());
    entity.setDisplayName(user.displayName());
    entity.setCreatedAt(user.createdAt());

    return entity;
  }
}
