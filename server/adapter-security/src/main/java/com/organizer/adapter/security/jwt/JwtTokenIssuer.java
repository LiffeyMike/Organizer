package com.organizer.adapter.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.organizer.application.port.out.RefreshTokenRepository;
import com.organizer.application.port.out.TokenIssuerPort;
import com.organizer.application.port.out.UserRepository;
import com.organizer.domain.auth.StoredRefreshToken;
import com.organizer.domain.auth.TokenPair;
import com.organizer.domain.user.Email;
import com.organizer.coreconfig.error.InvalidCredentialsException;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.FamilyId;
import com.organizer.coreconfig.id.RefreshTokenId;

@Component
public class JwtTokenIssuer implements TokenIssuerPort {

  private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
  private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);
  private static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";

  private final JwtSigner jwtSigner;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final Clock clock;
  private final SecureRandom secureRandom = new SecureRandom();

  public JwtTokenIssuer(JwtSigner jwtSigner, RefreshTokenRepository refreshTokenRepository,
      UserRepository userRepository, Clock clock) {
    this.jwtSigner = jwtSigner;
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
    this.clock = clock;
  }

  @Override
  public TokenPair issueInitial(UserId userId, Email email) {
    return issueAndPersist(userId, email, new FamilyId(UUID.randomUUID())).tokenPair();
  }

  @Override
  public Optional<UserId> parseAccessToken(String token) {
    return jwtSigner.verify(token)
        .flatMap(claims -> {
          try {
            return Optional.of(new UserId(UUID.fromString(claims.getSubject())));

          } catch (NullPointerException | IllegalArgumentException e) {
            return Optional.empty();
          }
        });
  }

  @Override
  public TokenPair rotateRefreshToken(String presentedRefreshToken) {
    StoredRefreshToken stored = refreshTokenRepository.findTokenByHash(hash(presentedRefreshToken))
        .orElseThrow(() -> new InvalidCredentialsException(INVALID_REFRESH_TOKEN));

    if (stored.isRevoked()) {
      refreshTokenRepository.revokeFamily(stored.familyId(), clock.instant());
      throw new InvalidCredentialsException(INVALID_REFRESH_TOKEN);
    }

    if (stored.isExpired(clock.instant())) {
      throw new InvalidCredentialsException(INVALID_REFRESH_TOKEN);
    }

    Email email = userRepository.findById(stored.userId())
        .map(user -> user.email())
        .orElseThrow(() -> new InvalidCredentialsException(INVALID_REFRESH_TOKEN));

    Issued issued = issueAndPersist(stored.userId(), email, stored.familyId());

    refreshTokenRepository.save(withRevocation(stored, clock.instant(), issued.refreshTokenId()));

    return issued.tokenPair();
  }

  private record Issued(TokenPair tokenPair, RefreshTokenId refreshTokenId) {
  }

  private Issued issueAndPersist(UserId userId, Email email, FamilyId familyId) {
    RefreshTokenId newId = new RefreshTokenId(UUID.randomUUID());
    String rawRefreshToken = generateOpaqueToken();
    Instant now = clock.instant();

    refreshTokenRepository.save(new StoredRefreshToken(
        newId,
        userId,
        familyId,
        hash(rawRefreshToken),
        now.plus(REFRESH_TOKEN_TTL),
        null,
        null,
        now));

    String accessToken = jwtSigner.sign(
        Map.of("sub", userId.id().toString(), "email", email.value()), ACCESS_TOKEN_TTL);

    return new Issued(new TokenPair(accessToken, rawRefreshToken), newId);
  }

  private StoredRefreshToken withRevocation(StoredRefreshToken original, Instant revokedAt,
      RefreshTokenId replacedById) {
    return new StoredRefreshToken(
        original.id(),
        original.userId(),
        original.familyId(),
        original.tokenHash(),
        original.expiresAt(),
        revokedAt,
        replacedById,
        original.createdAt());

  }

  private String generateOpaqueToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hash(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available on this JVM", e);
    }
  }
}
