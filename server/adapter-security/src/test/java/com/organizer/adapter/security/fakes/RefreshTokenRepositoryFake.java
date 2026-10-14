package com.organizer.adapter.security.fakes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.organizer.application.port.out.RefreshTokenRepository;
import com.organizer.coreconfig.id.FamilyId;
import com.organizer.coreconfig.id.RefreshTokenId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.auth.StoredRefreshToken;

public final class RefreshTokenRepositoryFake implements RefreshTokenRepository {

  private final Map<RefreshTokenId, StoredRefreshToken> rows = new LinkedHashMap<>();

  @Override
  public StoredRefreshToken save(StoredRefreshToken token) {
    rows.values().stream()
        .filter(existing -> existing.tokenHash().equals(token.tokenHash()))
        .filter(existing -> !existing.id().equals(token.id()))
        .findAny()
        .ifPresent(existing -> {
          throw new IllegalStateException("Duplicate token_hash: already stored on row " + existing.id());
        });

    rows.put(token.id(), token);
    return token;
  }

  @Override
  public Optional<StoredRefreshToken> findTokenByHash(String tokenHash) {
    return rows.values().stream()
        .filter(token -> token.tokenHash().equals(tokenHash))
        .findFirst();
  }

  @Override
  public void revokeFamily(FamilyId familyId, Instant revokedAt) {
    revokeWhere(token -> token.familyId().equals(familyId), revokedAt);
  }

  @Override
  public void revokeFamilyForUser(UserId userId, FamilyId familyId, Instant revokedAt) {
    revokeWhere(token -> token.familyId().equals(familyId) && token.userId().equals(userId), revokedAt);
  }

  public List<StoredRefreshToken> all() {
    return List.copyOf(rows.values());
  }

  public StoredRefreshToken byId(RefreshTokenId id) {
    StoredRefreshToken row = rows.get(id);
    if (row == null) {
      throw new AssertionError("No refresh token row with id: " + id);
    }
    return row;
  }

  public StoredRefreshToken byHash(String tokenHash) {
    return findTokenByHash(tokenHash)
        .orElseThrow(() -> new AssertionError("No refresh token with hash " + tokenHash));
  }

  public List<StoredRefreshToken> inFamily(FamilyId familyId) {
    return rows.values().stream()
        .filter(token -> token.familyId().equals(familyId))
        .toList();
  }

  private void revokeWhere(Predicate<StoredRefreshToken> matches, Instant revokedAt) {
    rows.replaceAll((id, token) -> matches.test(token) && !token.isRevoked()
        ? withRevokedAt(token, revokedAt)
        : token);
  }

  private static StoredRefreshToken withRevokedAt(StoredRefreshToken token, Instant revokedAt) {
    return new StoredRefreshToken(
        token.id(),
        token.userId(),
        token.familyId(),
        token.tokenHash(),
        token.expiresAt(),
        revokedAt,
        token.replacedById(),
        token.createdAt());
  }

}
