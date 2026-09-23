package com.organizer.adapter.persistence.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

interface JpaRefeshTokenRepository extends JpaRepository<JpaRefreshToken, UUID> {
  Optional<JpaRefreshToken> findByTokenHash(String tokenHash);

  @Transactional
  @Modifying
  @Query("""
        UPDATE JpaRefreshToken t SET t.revokedAt = :revokedAt
          WHERE t.familyId = :familyId AND t.revokedAt IS NULL
      """)
  void revokeFamily(@Param("familyId") UUID familyId, @Param("revokedAt") Instant revokedAt);

  @Transactional
  @Modifying
  @Query("""
        UPDATE JpaRefreshToken t SET t.revokedAt = :revokedAt
          WHERE t.familyId = :familyId AND t.userId = :userId and t.revokedAt IS NULL
      """)
  void revokeFamilyForUser(@Param("userId") UUID userId, @Param("familyId") UUID familyId,
      @Param("revokedAt") Instant revokedAt);
}
