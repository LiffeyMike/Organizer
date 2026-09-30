package com.organizer.application.port.out;

import com.organizer.domain.user.HashedPassword;

public interface PasswordHasherPort {
  HashedPassword hash(String rawPassword);

  Boolean matches(String rawPassword, HashedPassword hash);
}
