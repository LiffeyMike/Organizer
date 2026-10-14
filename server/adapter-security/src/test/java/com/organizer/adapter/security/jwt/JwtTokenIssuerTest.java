package com.organizer.adapter.security.jwt;

import java.util.UUID;

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

  @Test
  void roundTrip() {
    TokenPair pair = issuer.issueInitial(userId, email);
    assertThat(issuer.parseAccessToken(pair.accessToken())).contains(userId);
  }

}
