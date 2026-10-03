package com.organizer.adapter.security.jwt;

import java.time.Duration;
import java.time.Instant;
import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;

@Component
public class JwtSigner {
  private final RsaKeyPairHolder keyPairHolder;
  private final Clock clock;

  public JwtSigner(RsaKeyPairHolder keyPairHolder, Clock clock) {
    this.keyPairHolder = keyPairHolder;
    this.clock = clock;
  }

  public String sign(Map<String, Object> claims, Duration ttl) {
    Instant now = clock.instant();
    return Jwts.builder()
        .claims(claims)
        .issuer("organizer")
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(ttl)))
        .id(UUID.randomUUID().toString())
        .signWith(keyPairHolder.privateKey(), Jwts.SIG.RS256)
        .compact();
  }

  public Optional<Claims> verify(String token) {
    try {
      Jws<Claims> parsed = Jwts.parser()
          .clock(() -> Date.from(clock.instant()))
          .verifyWith(keyPairHolder.publicKey())
          .build()
          .parseSignedClaims(token);
      return Optional.of(parsed.getPayload());
    } catch (JwtException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
