package com.organizer.adapter.graphql.dto;

import com.organizer.adapter.graphql.error.MutationResult;
import com.organizer.adapter.graphql.error.UserError;
import com.organizer.application.user.AuthResult;
import java.util.List;

public record UserRegisterPayload(User user, Session session, List<UserError> userErrors) {

  public static UserRegisterPayload from(MutationResult<AuthResult> result) {
    AuthResult value = result.value();
    return new UserRegisterPayload(
        value == null ? null : User.from(value.user()),
        value == null ? null : Session.from(value),
        result.userErrors());
  }
}
