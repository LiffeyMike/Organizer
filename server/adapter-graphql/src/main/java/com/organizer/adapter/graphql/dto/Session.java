package com.organizer.adapter.graphql.dto;

import com.organizer.application.user.AuthResult;

public record Session(String accessToken, String refreshToken, User user) {

  public static Session from(AuthResult result) {
    return new Session(
        result.tokens().accessToken(), result.tokens().refreshToken(), User.from(result.user()));
  }
}
