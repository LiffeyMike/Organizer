package com.organizer.adapter.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentPrincipal {

  private CurrentPrincipal() {}

  public static Optional<AuthenticatedUser> get() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    return authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user
        ? Optional.of(user)
        : Optional.empty();
  }
}
