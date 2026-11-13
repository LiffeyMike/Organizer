package com.organizer.adapter.graphql.dto;

import com.organizer.application.user.RegisterCommand;

public record UserRegisterInput(String email, String password, String displayName) {

  public RegisterCommand toCommand() {
    return new RegisterCommand(email, password, displayName);
  }

  @Override
  public String toString() {
    return "UserRegisterInput[email=" + email + ", password=***, displayName=" + displayName + "]";
  }
}
