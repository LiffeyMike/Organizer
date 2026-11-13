package com.organizer.adapter.graphql.dto;

public record SessionRefreshInput(String refreshToken) {

  public String toCommand() {
    return refreshToken;
  }

  @Override
  public String toString() {
    return "SessionRefreshInput[refreshToken=***]";
  }
}
