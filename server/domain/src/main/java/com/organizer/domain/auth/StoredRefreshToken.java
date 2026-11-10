package com.organizer.domain.auth;

import com.organizer.coreconfig.id.FamilyId;
import com.organizer.coreconfig.id.RefreshTokenId;
import com.organizer.coreconfig.id.UserId;
import java.time.Instant;

public record StoredRefreshToken(
    RefreshTokenId id,
    UserId userId,
    FamilyId familyId,
    String tokenHash,
    Instant expiresAt,
    Instant revokedAt,
    RefreshTokenId replacedById,
    Instant createdAt) {

  public boolean isRevoked() {
    return revokedAt != null;
  }

  public boolean isExpired(Instant now) {
    return now.isAfter(expiresAt);
  }
}
