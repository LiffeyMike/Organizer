package com.organizer.adapter.security.jwt;

import java.util.UUID;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import java.util.Map;
import java.util.HexFormat;
import java.util.Date;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import com.organizer.adapter.security.support.MutableClock;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.User;
import com.organizer.domain.user.Email;
import com.organizer.domain.auth.StoredRefreshToken;
import com.organizer.domain.auth.TokenPair;
import com.organizer.adapter.security.fakes.RefreshTokenRepositoryFake;
import com.organizer.adapter.security.fakes.UserRepositoryFake;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenIssuerTest {
  private static final RsaKeyPairHolder KEYS = new RsaKeyPairHolder();

  private final UserId userId = new UserId(UUID.randomUUID());
  private final Email email = new Email("elliot@fsociety.net");

  private MutableClock clock;
  private JwtSigner signer;
  private RefreshTokenRepositoryFake tokens;
  private UserRepositoryFake users;
  private JwtTokenIssuer issuer;

  @BeforeEach
  void setup() {
    clock = new MutableClock();
    signer = new JwtSigner(KEYS, clock);
    tokens = new RefreshTokenRepositoryFake();
    users = new UserRepositoryFake();
    issuer = new JwtTokenIssuer(signer, tokens, users, clock);

    User user = new User(
        userId,
        email,
        new HashedPassword("irrelevant"),
        "Elliot",
        clock.instant());

    users.save(user);
  }

  @Nested
  class AccessToken {

    @Test
    void roundTrip() {
      TokenPair pair = issuer.issueInitial(userId, email);
      assertThat(issuer.parseAccessToken(pair.accessToken())).contains(userId);
    }

    @Test
    void claimsAreWhatCallersRelyOn() {
      Instant now = clock.instant();
      TokenPair pair = issuer.issueInitial(userId, email);

      Claims claims = signer.verify(pair.accessToken()).orElseThrow();

      assertThat(claims.getSubject()).isEqualTo(userId.id().toString());
      assertThat(claims.get("email", String.class)).isEqualTo("elliot@fsociety.net");
      assertThat(claims.getIssuer()).isEqualTo("organizer");
      assertThat(claims.getId()).isNotBlank();
      assertThat(claims.getIssuedAt().toInstant()).isEqualTo(now);
      assertThat(claims.getExpiration().toInstant()).isEqualTo(now.plus(Duration.ofMinutes(15)));
    }

    @Test
    void stillValidJustBeforeExpiry() {
      TokenPair pair = issuer.issueInitial(userId, email);
      clock.advance(Duration.ofMinutes(15).minusSeconds(1));
      assertThat(issuer.parseAccessToken(pair.accessToken())).contains(userId);
    }

    @Test
    void expired() {
      TokenPair pair = issuer.issueInitial(userId, email);
      clock.advance(Duration.ofMinutes(15).plusSeconds(1));

      assertThat(issuer.parseAccessToken(pair.accessToken())).isEmpty();
    }

    @Test
    void tamperedSignature() {
      String[] parts = issuer.issueInitial(userId, email).accessToken().split("\\.");
      char[] signature = parts[2].toCharArray();
      int middle = signature.length / 2;
      signature[middle] = signature[middle] == 'A' ? 'B' : 'A';

      String tampered = parts[0] + "." + parts[1] + "." + new String(signature);

      assertThat(issuer.parseAccessToken(tampered)).isEmpty();
    }

    @Test
    void tamperedPayloadSwapsSubject() {
      String parts[] = issuer.issueInitial(userId, email).accessToken().split("\\.");
      String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
      String victim = UUID.randomUUID().toString();
      String swapped = payload.replace(userId.id().toString(), victim);
      assertThat(swapped).contains(victim);

      String tampered = parts[0] + "."
          + Base64.getUrlEncoder().withoutPadding().encodeToString(swapped.getBytes(StandardCharsets.UTF_8))
          + "." + parts[2];

      assertThat(issuer.parseAccessToken(tampered)).isEmpty();
    }

    @Test
    void signedByDifferentRsaKet() {
      JwtSigner otherSigner = new JwtSigner(new RsaKeyPairHolder(), clock);
      String foreign = otherSigner.sign(
          Map.of("sub", userId.id().toString(), "email", email.value()), Duration.ofMinutes(15));

      assertThat(issuer.parseAccessToken(foreign)).isEmpty();
    }

  }

  @Nested
  class AlgorithmConfusion {

    private JwtBuilder otherwiseValidToken() {
      Instant now = clock.instant();
      return Jwts.builder()
          .subject(userId.id().toString())
          .claim("email", email.value())
          .issuer("organizer")
          .issuedAt(Date.from(now))
          .expiration(Date.from(now.plus(Duration.ofMinutes(15))));
    }

    @Test
    void hs256WithAttackerSecret() {
      byte[] secret = new byte[32];
      new SecureRandom().nextBytes(secret);

      String token = otherwiseValidToken()
          .signWith(Keys.hmacShaKeyFor(secret), Jwts.SIG.HS256)
          .compact();

      assertThat(issuer.parseAccessToken(token)).isEmpty();
    }

    @Test
    void hs256KeyedWithServerPublicKey() {
      SecretKeySpec publicKeyAsHmacSecret = new SecretKeySpec(KEYS.publicKey().getEncoded(), "HmacSha256");

      String token = otherwiseValidToken()
          .signWith(publicKeyAsHmacSecret, Jwts.SIG.HS256)
          .compact();

      assertThat(issuer.parseAccessToken(token)).isEmpty();
    }

    @Test
    void algNone() {
      String token = otherwiseValidToken().compact();

      assertThat(token).endsWith(".");

      assertThat(issuer.parseAccessToken(token)).isEmpty();
    }

  }

  @Nested
  class initialIssuance {

    @Test
    void persistsExactlyOneRowWithTheRightShape() {
      Instant now = clock.instant();
      TokenPair pair = issuer.issueInitial(userId, email);

      assertThat(tokens.all()).hasSize(1);
      StoredRefreshToken row = tokens.all().get(0);

      assertThat(row.userId()).isEqualTo(userId);
      assertThat(row.familyId()).isNotNull();
      assertThat(row.tokenHash()).isEqualTo(sha256Hex(pair.refreshToken()));
      assertThat(row.createdAt()).isEqualTo(now);
      assertThat(row.expiresAt()).isEqualTo(now.plus(Duration.ofDays(30)));
      assertThat(row.revokedAt()).isNull();
      assertThat(row.replacedById()).isNull();
    }

    @Test
    void rawRefreshTokenIsNeverStored() {
      TokenPair pair = issuer.issueInitial(userId, email);
      assertThat(tokens.all())
          .extracting(StoredRefreshToken::tokenHash)
          .doesNotContain(pair.refreshToken());
    }

    @Test
    void refreshTokenShape() {
      TokenPair pair = issuer.issueInitial(userId, email);
      assertThat(pair.refreshToken()).matches("^[A-Za-z0-9_-]{43}$");
    }

    @Test
    void eachLoginStartsItsOwnFamily() {
      TokenPair first = issuer.issueInitial(userId, email);
      TokenPair second = issuer.issueInitial(userId, email);

      assertThat(tokens.all()).hasSize(2);
      assertThat(tokens.byHash(sha256Hex(first.refreshToken())).familyId())
          .isNotEqualTo(tokens.byHash(sha256Hex(second.refreshToken())).familyId());
      assertThat(first.refreshToken()).isNotEqualTo(second.refreshToken());
    }
  }

  @Nested
  class RotationHappyPath {

    @Test
    void producesAFreshWorkingPair() {
      TokenPair original = issuer.issueInitial(userId, email);
      TokenPair rotated = issuer.rotateRefreshToken(original.refreshToken());

      assertThat(rotated.refreshToken()).isNotEqualTo(original.refreshToken());
      assertThat(issuer.parseAccessToken(rotated.accessToken())).contains(userId);
    }

    @Test
    void storedStateAfterOneRotation() {
      TokenPair original = issuer.issueInitial(userId, email);
      StoredRefreshToken before = tokens.byHash(sha256Hex(original.refreshToken()));

      clock.advance(Duration.ofMinutes(5));
      Instant rotatedAt = clock.instant();
      TokenPair rotated = issuer.rotateRefreshToken(original.refreshToken());

      StoredRefreshToken oldRow = tokens.byId(before.id());
      StoredRefreshToken newRow = tokens.byHash(sha256Hex(rotated.refreshToken()));

      assertThat(tokens.all()).hasSize(2);
      assertThat(newRow.familyId()).isEqualTo(oldRow.familyId());

      assertThat(oldRow.revokedAt()).isEqualTo(rotatedAt);
      assertThat(oldRow.replacedById()).isEqualTo(newRow.id());
      assertThat(oldRow.tokenHash()).isEqualTo(before.tokenHash());
      assertThat(oldRow.expiresAt()).isEqualTo(before.expiresAt());
      assertThat(oldRow.createdAt()).isEqualTo(before.createdAt());

      assertThat(newRow.revokedAt()).isNull();
      assertThat(newRow.replacedById()).isNull();
    }

    @Test
    void newRefreshTokenGetsAFullNewTtl() {
      Instant start = clock.instant();
      TokenPair original = issuer.issueInitial(userId, email);

      clock.advance(Duration.ofDays(10));
      TokenPair rotated = issuer.rotateRefreshToken(original.refreshToken());

      assertThat(tokens.byHash(sha256Hex(rotated.refreshToken())).expiresAt())
          .isEqualTo(start.plus(Duration.ofDays(10).plus(Duration.ofDays(30))));
    }

    @Test
    void emailComesFromUserRepositoryNotOldToken() {
      TokenPair original = issuer.issueInitial(userId, email);
      users.save(new User(
          userId,
          new Email("mr.robot@fsociety.net"),
          new HashedPassword("doesntmatter"),
          "Elliot",
          clock.instant()));

      TokenPair rotated = issuer.rotateRefreshToken(original.refreshToken());

      Claims claims = signer.verify(rotated.accessToken()).orElseThrow();
      assertThat(claims.get("email", String.class)).isEqualTo("mr.robot@fsociety.net");
    }

    @Test
    void chainOfRotations() {
      TokenPair a = issuer.issueInitial(userId, email);
      TokenPair b = issuer.rotateRefreshToken(a.refreshToken());
      TokenPair c = issuer.rotateRefreshToken(b.refreshToken());
      TokenPair d = issuer.rotateRefreshToken(c.refreshToken());

      StoredRefreshToken rowA = tokens.byHash(sha256Hex(a.refreshToken()));
      StoredRefreshToken rowB = tokens.byHash(sha256Hex(b.refreshToken()));
      StoredRefreshToken rowC = tokens.byHash(sha256Hex(c.refreshToken()));
      StoredRefreshToken rowD = tokens.byHash(sha256Hex(d.refreshToken()));

      assertThat(tokens.all()).hasSize(4);
      assertThat(tokens.inFamily(rowA.familyId())).hasSize(4);

      assertThat(rowA.replacedById()).isEqualTo(rowB.id());
      assertThat(rowB.replacedById()).isEqualTo(rowC.id());
      assertThat(rowC.replacedById()).isEqualTo(rowD.id());
      assertThat(rowD.replacedById()).isNull();

      assertThat(List.of(rowA, rowB, rowC)).allSatisfy(row -> assertThat(row.isRevoked()).isTrue());
      assertThat(rowD.isRevoked()).isFalse();
    }

    @Test
    void justInsideExpiryStillRotates() {
      TokenPair original = issuer.issueInitial(userId, email);
      clock.advance(Duration.ofDays(30));

      TokenPair rotated = issuer.rotateRefreshToken(original.refreshToken());

      assertThat(issuer.parseAccessToken(rotated.accessToken())).contains(userId);
    }

  }

  private static String sha256Hex(String raw) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

}
