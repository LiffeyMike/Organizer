package com.organizer.adapter.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentPrincipalTest {

  private final AuthenticatedUser user =
      new AuthenticatedUser(new UserId(UUID.randomUUID()), new Email("user@server.net"));

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void returnsTheAuthenticatedUser() {
    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    assertThat(CurrentPrincipal.get()).contains(user);
  }

  @Test
  void anonymousRequestIsEmpty() {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    SecurityContextHolder.setContext(context);

    assertThat(CurrentPrincipal.get()).isEmpty();
  }

  @Test
  void noAuthenticationIsEmpty() {
    assertThat(CurrentPrincipal.get()).isEmpty();
  }
}
