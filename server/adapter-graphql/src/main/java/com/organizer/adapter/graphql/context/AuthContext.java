package com.organizer.adapter.graphql.context;

import com.organizer.adapter.security.AuthenticatedUser;
import com.organizer.coreconfig.error.UnauthenticatedException;
import com.organizer.coreconfig.id.UserId;

public final class AuthContext {

  public static final String PRINCIPAL_KEY = "authenticatedUser";

  private AuthContext() {}

  public static UserId requireUserId(AuthenticatedUser principal) {
    if (principal == null) {
      throw new UnauthenticatedException("Authentication required");
    }
    return principal.userId();
  }
}
