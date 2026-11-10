package com.organizer.adapter.security.config;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.organizer.adapter.security.AuthenticatedUser;
import com.organizer.adapter.security.jwt.JwtSigner;
import com.organizer.adapter.security.jwt.RsaKeyPairHolder;
import com.organizer.adapter.security.support.MutableClock;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;

import io.jsonwebtoken.Jwts;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

  private static final RsaKeyPairHolder KEYS = new RsaKeyPairHolder();
  private static final Duration TTL = Duration.ofMinutes(15);

  private final UserId userId = new UserId(UUID.randomUUID());
  private final Email email = new Email("elliot@fsociety.net");
  private final AuthenticatedUser correctPrincipal = new AuthenticatedUser(userId, email);

  private MutableClock clock;
  private JwtSigner signer;
  private JwtAuthenticationFilter filter;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private MockFilterChain chain;

  @BeforeEach
  void setup() {
    clock = new MutableClock();
    signer = new JwtSigner(KEYS, clock);
    filter = new JwtAuthenticationFilter(signer);

    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    chain = new MockFilterChain();
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void validBearerTokenSetsAuthenticatedUserPrincipal() throws Exception {
    request.addHeader("Authorization", "Bearer " + validToken());

    filter.doFilter(request, response, chain);

    Authentication authentication = authentication();
    assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
    assertThat(authentication.getPrincipal()).isEqualTo(correctPrincipal);
    assertThat(authentication.getCredentials()).isNull();
    assertThat(authentication.getAuthorities()).isEmpty();
    assertThat(authentication.isAuthenticated()).isTrue();
    assertChainInvoked();
  }

  @Test
  void noAuthorizationHeaderLeavesContextEmpty() throws Exception {
    filter.doFilter(request, response, chain);

    assertThat(authentication()).isNull();
    assertChainInvoked();
  }

  @ParameterizedTest
  @ValueSource(strings = { "Basic fkdajlsfjl", "Bearer ", "Bearer useless" })
  void unusableHeaderLeavesContextEmpty(String header) throws Exception {
    request.addHeader("Authorization", header);

    filter.doFilter(request, response, chain);

    assertThat(authentication()).isNull();
    assertChainInvoked();
  }

  @Test
  void expiredTokenLeavesContextEmpty() throws Exception {
    String token = validToken();
    clock.advance(TTL.plusSeconds(1));
    request.addHeader("Authorization", "Bearer " + token);

    filter.doFilter(request, response, chain);

    assertThat(authentication()).isNull();
    assertChainInvoked();
  }

  @Test
  void hs256TokenKeyedWithServerPublicKeyLeavesContextEmpty() throws Exception {
    Instant now = clock.instant();
    String token = Jwts.builder()
        .subject(userId.id().toString())
        .claim("email", email.value())
        .issuer("organizer")
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(TTL)))
        .signWith(new SecretKeySpec(KEYS.publicKey().getEncoded(), "HmacSHA256"), Jwts.SIG.HS256)
        .compact();

    request.addHeader("Authorization", "Bearer " + token);

    filter.doFilter(request, response, chain);

    assertThat(authentication()).isNull();
    assertChainInvoked();
  }

  @ParameterizedTest
  @ValueSource(strings = { "bearer ", "BEARER " })
  void schemeIsCaseInsensitive(String scheme) throws Exception {
    request.addHeader("Authorization", scheme + validToken());

    filter.doFilter(request, response, chain);

    assertThat(authentication()).isNotNull();
    assertThat(authentication().getPrincipal()).isEqualTo(correctPrincipal);
    assertChainInvoked();
  }

  @ParameterizedTest
  @MethodSource("badClaims")
  void validlySignedTokenWithBadClaims(Map<String, Object> claims) throws Exception {
    request.addHeader("Authorization", "Bearer " + signer.sign(claims, TTL));

    filter.doFilter(request, response, chain);

    assertThat(authentication()).isNull();
    assertChainInvoked();
  }

  static Stream<Map<String, Object>> badClaims() {
    String sub = UUID.randomUUID().toString();
    return Stream.of(
        Map.of("sub", sub),
        Map.of("sub", sub, "email", "not-an-email"),
        Map.of("sub", sub, "email", 42),
        Map.of("email", "elliot@fsociety.net"),
        Map.of("sub", "not-a-uuid", "email", "elliot@fsociety.net"));
  }

  private String validToken() {
    return signer.sign(Map.of("sub", userId.id().toString(), "email", email.value()), TTL);
  }

  private Authentication authentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  private void assertChainInvoked() {
    assertThat(chain.getRequest()).isSameAs(request);
  }

}
