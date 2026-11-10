package com.organizer.application.port.out;

import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.User;
import java.util.Optional;

public interface UserRepository {
  User save(User user);

  Optional<User> findByEmail(Email email);

  Optional<User> findById(UserId id);

  boolean existsByEmail(Email email);
}
