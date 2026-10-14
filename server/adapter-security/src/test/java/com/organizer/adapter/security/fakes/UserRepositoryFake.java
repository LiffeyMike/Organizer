package com.organizer.adapter.security.fakes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.organizer.application.port.out.UserRepository;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.User;

public final class UserRepositoryFake implements UserRepository {

  private final Map<UserId, User> users = new HashMap<>();

  @Override
  public User save(User user) {
    users.put(user.id(), user);
    return user;
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return users.values().stream()
        .filter(user -> user.email().equals(email))
        .findFirst();
  }

  @Override
  public Optional<User> findById(UserId userId) {
    return Optional.ofNullable(users.get(userId));
  }

  @Override
  public boolean existsByEmail(Email email) {
    return findByEmail(email).isPresent();
  }

  public void delete(UserId id) {
    users.remove(id);
  }
}
