package com.organizer.adapter.persistence.auth;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.organizer.adapter.persistence.AbstractIntegrationTest;
import com.organizer.adapter.persistence.user.UserRepositoryAdapter;
import com.organizer.coreconfig.id.FamilyId;
import com.organizer.coreconfig.id.RefreshTokenId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.auth.StoredRefreshToken;
import com.organizer.domain.user.User;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.Email;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenRepositoryAdapterTest extends AbstractIntegrationTest {
  private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

  @Autowired
  UserRepositoryAdapter users;

  @Autowired
  RefreshTokenRepositoryAdapter tokens;

  private UserId userId;

  @BeforeEach
  void createUser() {
    UserId userId = new UserId(UUID.randomUUID());
    Email userEmail = new Email("bob@building.net");
    HashedPassword passwd = new HashedPassword("h4sh3d");
    String userName = "Bob the builder";
    this.userId = users.save(User.register(
        userId,
        userEmail,
        passwd,
        userName,
        fixedClock)).id();
  }

  @Test
  void savesAndFindsByTokenHash() {
    StoredRefreshToken token = tokens.save(newToken(new FamilyId(UUID.randomUUID()), "hash-1"));

    assertThat(tokens.findTokenByHash("hash-1")).contains(token);
  }

  @Test
  void revokeFamilyRevokesEveryTokenInTheFamily() {
    FamilyId familyId = new FamilyId(UUID.randomUUID());
    tokens.save(newToken(familyId, "hash-1"));
    tokens.save(newToken(familyId, "hash-2"));
    tokens.save(newToken(familyId, "hash-3"));

    tokens.revokeFamily(familyId, fixedClock.instant());

    assertThat(tokens.findTokenByHash("hash-1")
        .orElseThrow().isRevoked()).isTrue();
    assertThat(tokens.findTokenByHash("hash-2")
        .orElseThrow().isRevoked()).isTrue();
    assertThat(tokens.findTokenByHash("hash-3")
        .orElseThrow().isRevoked()).isTrue();
  }

  @Test
  void revokeFamilyForUserRevokesTheGivenUsersTokens() {
    FamilyId familyId = new FamilyId(UUID.randomUUID());
    tokens.save(newToken(familyId, "hash-1"));

    tokens.revokeFamilyForUser(
        userId,
        familyId,
        fixedClock.instant());

    assertThat(tokens.findTokenByHash("hash-1")
        .orElseThrow().isRevoked()).isTrue();
  }

  private StoredRefreshToken newToken(FamilyId familyId, String tokenHash) {
    return new StoredRefreshToken(new RefreshTokenId(UUID.randomUUID()), userId, familyId, tokenHash,
        fixedClock.instant().plusSeconds(3600), null, null, fixedClock.instant());

  }
}
