package com.organizer.adapter.persistence.auth;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.organizer.coreconfig.id.RefreshTokenId;
import com.organizer.coreconfig.id.FamilyId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.auth.StoredRefreshToken;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenMapperTest {

  private final RefreshTokenMapper mapper = new RefreshTokenMapper();
  private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

  @Test
  void roundTripsAnActiveToken() {
    StoredRefreshToken token = new StoredRefreshToken(
        new RefreshTokenId(UUID.randomUUID()),
        new UserId(UUID.randomUUID()),
        new FamilyId(UUID.randomUUID()),
        "hash-1",
        fixedClock.instant().plus(3600, ChronoUnit.SECONDS),
        null,
        null,
        fixedClock.instant());

    assertThat(mapper.toDomain(mapper.toEntity(token))).isEqualTo(token);
  }

  @Test
  void roundTripsARevokedToken() {
    StoredRefreshToken token = new StoredRefreshToken(
        new RefreshTokenId(UUID.randomUUID()),
        new UserId(UUID.randomUUID()),
        new FamilyId(UUID.randomUUID()),
        "hash-1",
        fixedClock.instant().plus(3600, ChronoUnit.SECONDS),
        fixedClock.instant().plus(60, ChronoUnit.SECONDS),
        new RefreshTokenId(UUID.randomUUID()),
        fixedClock.instant());

    assertThat(mapper.toDomain(mapper.toEntity(token))).isEqualTo(token);
  }
}
