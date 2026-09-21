package com.organizer.domain.user;

import java.time.Clock;
import java.time.Instant;

import com.organizer.coreconfig.error.ValidationException;
import com.organizer.coreconfig.id.UserId;

public record User(UserId id, Email email, HashedPassword password, String displayName, Instant createdAt) {

  public static User register(UserId id, Email email, HashedPassword password, String displayName, Clock clock) {
    if (displayName == null || displayName.isBlank()) {
      throw new ValidationException("Display name required");
    }
    return new User(id, email, password, displayName.trim(), clock.instant());
  }
}
