package com.organizer.adapter.persistence.user;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.organizer.adapter.persistence.AbstractIntegrationTest;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryAdapaterTest extends AbstractIntegrationTest {

  private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

  @Autowired
  UserRepositoryAdapter adapter;

  @Test
  void savesAndFindsByEmailAndId() {
    UserId userId = new UserId(UUID.randomUUID());
    Email userEmail = new Email("ada@computers.net");
    HashedPassword userPassword = new HashedPassword("h4sh3d");
    String displayName = "Ada";

    User user = User.register(
        userId, userEmail, userPassword, displayName, fixedClock);

    User saved = adapter.save(user);

    assertThat(adapter.findByEmail(userEmail)).contains(saved);
    assertThat(adapter.findById(userId)).contains(saved);
  }

  @Test
  void existsByEmailReflectsSaveState() {
    UserId userId = new UserId(UUID.randomUUID());
    Email email = new Email("starlord@guardians.gxy");
    HashedPassword userPassword = new HashedPassword("h4sh3d");
    String displayName = "Ada";

    assertThat(adapter.existsByEmail(email)).isFalse();

    adapter.save(User.register(userId, email, userPassword, displayName, fixedClock));

    assertThat(adapter.existsByEmail(email)).isTrue();
  }

}
