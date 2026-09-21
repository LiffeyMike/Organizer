package com.organizer.domain.user;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.organizer.coreconfig.error.ValidationException;
import com.organizer.coreconfig.id.UserId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {
  private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

  @Test
  void registerRejectsBlankDisplayName() {
    assertThatThrownBy(() -> User.register(
        new UserId(UUID.randomUUID()), new Email("apple@basket.com"), new HashedPassword("h4sh3d"), "   ", fixedClock))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void registerTrimsDisplayNameAndStampsCreatedAt() {
    User user = User.register(
        new UserId(UUID.randomUUID()), new Email("pear@basket.com"), new HashedPassword("h4sh3d"), "   Ada   ",
        fixedClock);

    assertThat(user.displayName()).isEqualTo("Ada");
    assertThat(user.createdAt()).isEqualTo(fixedClock.instant());
  }
}
