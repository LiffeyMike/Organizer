package com.organizer.adapter.graphql.error;

public class BadUserInputException extends RuntimeException {
  public BadUserInputException(String message) {
    super(message);
  }
}
