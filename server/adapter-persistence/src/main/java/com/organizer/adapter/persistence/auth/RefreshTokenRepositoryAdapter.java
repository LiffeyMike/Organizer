package com.organizer.adapter.persistence.auth;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.organizer.coreconfig.id.FamilyId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.application.port.out.RefreshTokenRepository;
import com.organizer.domain.auth.StoredRefreshToken;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

  private final JpaRefreshTokenRepository repo;
  private final RefreshTokenMapper mapper;

  public RefreshTokenRepositoryAdapter(JpaRefreshTokenRepository repo, RefreshTokenMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  @Override
  public StoredRefreshToken save(StoredRefreshToken token) {
    return mapper.toDomain(
        repo.save(mapper.toEntity(token)));
  }

  @Override
  public Optional<StoredRefreshToken> findTokenByHash(String tokenHash) {
    return repo.findByTokenHash(tokenHash).map(mapper::toDomain);
  }

  @Override
  public void revokeFamily(FamilyId familyId, Instant revokedAt) {
    repo.revokeFamily(familyId.id(), revokedAt);
  }

  @Override
  public void revokeFamilyForUser(UserId userId, FamilyId familyId, Instant revokedAt) {
    repo.revokeFamilyForUser(userId.id(), familyId.id(), revokedAt);
  }

}
