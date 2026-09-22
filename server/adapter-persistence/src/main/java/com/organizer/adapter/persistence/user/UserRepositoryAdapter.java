package com.organizer.adapter.persistence.user;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.organizer.application.port.out.UserRepository;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.User;

@Component
public class UserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaRepository;
  private final UserMapper mapper;

  public UserRepositoryAdapter(JpaUserRepository jpaRepository, UserMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public User save(User user) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return jpaRepository.findByEmail(email.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<User> findById(UserId id) {
    return jpaRepository.findById(id.id()).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmail(Email email) {
    return jpaRepository.existsByEmail(email.value());
  }
}
