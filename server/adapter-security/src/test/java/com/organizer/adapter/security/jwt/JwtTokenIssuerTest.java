package com.organizer.adapter.security.jwt;

import java.util.UUID;
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

import com.organizer.adapter.security.support.MutableClock;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.User;
import com.organizer.domain.user.Email;
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

  private static String sha256Hex(String raw) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

}
