package com.organizer.application.port.out;

import com.organizer.coreconfig.id.FamilyId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.auth.StoredRefreshToken;
import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {

  StoredRefreshToken save(StoredRefreshToken token);

  Optional<StoredRefreshToken> findTokenByHash(String tokenHash);

  void revokeFamily(FamilyId familyId, Instant revokedAt);

  void revokeFamilyForUser(UserId userId, FamilyId familyId, Instant revokedAt);
}
