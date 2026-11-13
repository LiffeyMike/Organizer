package com.organizer.adapter.graphql.dto;

import com.organizer.adapter.graphql.error.MutationResult;
import com.organizer.adapter.graphql.error.UserError;
import com.organizer.application.user.AuthResult;
import java.util.List;

public record SessionCreatePayload(Session session, List<UserError> userErrors) {

  public static SessionCreatePayload from(MutationResult<AuthResult> result) {
    AuthResult value = result.value();
    return new SessionCreatePayload(
        value == null ? null : Session.from(value), result.userErrors());
  }
}
