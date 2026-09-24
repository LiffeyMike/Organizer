package com.organizer.adapter.persistence.auth;

import org.springframework.stereotype.Component;

import java.time.Instant;

import com.organizer.coreconfig.id.RefreshTokenId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.FamilyId;
import com.organizer.domain.auth.StoredRefreshToken;

@Component
public class RefreshTokenMapper {

  public JpaRefreshToken toEntity(StoredRefreshToken token) {
    JpaRefreshToken entity = new JpaRefreshToken();
    entity.setId(token.id().id());
    entity.setUserId(token.userId().id());
    entity.setFamilyId(token.familyId().id());
    entity.setTokenHash(token.tokenHash());
    entity.setExpiresAt(token.expiresAt());
    entity.setRevokedAt(token.revokedAt());
    entity.setReplacedById(token.replacedById() == null ? null : token.replacedById().id());
    entity.setCreatedAt(token.createdAt());

    return entity;
  }

  public StoredRefreshToken toDomain(JpaRefreshToken entity) {
    RefreshTokenId id = new RefreshTokenId(entity.getId());
    UserId userId = new UserId(entity.getUserId());
    FamilyId familyId = new FamilyId(entity.getFamilyId());
    String tokenHash = entity.getTokenHash();
    Instant expiresAt = entity.getExpiresAt();
    Instant revokedAt = entity.getRevokedAt();
    RefreshTokenId replacedById = entity.getReplacedById() == null ? null
        : new RefreshTokenId(entity.getReplacedById());
    Instant createdAt = entity.getCreatedAt();

    StoredRefreshToken token = new StoredRefreshToken(
        id, userId, familyId, tokenHash, expiresAt, revokedAt, replacedById, createdAt);

    return token;
  }
}
