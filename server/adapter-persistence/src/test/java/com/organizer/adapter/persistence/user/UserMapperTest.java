package com.organizer.adapter.persistence.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.User;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserMapperTest {

  private final UserMapper mapper = new UserMapper();

  @Test
  void roundTripsEveryField() {
    User user =
        new User(
            new UserId(UUID.randomUUID()),
            new Email("healthy@man.ie"),
            new HashedPassword("h4sh3d"),
            "Strong man",
            Instant.parse("2026-01-01T00:00:00Z"));

    User roundTripped = mapper.toDomain(mapper.toEntity(user));

    assertThat(roundTripped).isEqualTo(user);
  }
}
