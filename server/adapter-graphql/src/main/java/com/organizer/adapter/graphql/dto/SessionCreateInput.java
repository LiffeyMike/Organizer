package com.organizer.adapter.graphql.dto;

import com.organizer.application.user.LoginCommand;

public record SessionCreateInput(String email, String password) {

  public LoginCommand toCommand() {
    return new LoginCommand(email, password);
  }

  @Override
  public String toString() {
    return "SessionCreateInput[email=" + email + ", password=***]";
  }
}
