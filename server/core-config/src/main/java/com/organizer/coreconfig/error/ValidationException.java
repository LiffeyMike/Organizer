package com.organizer.coreconfig.error;

public class ValidationException extends DomainException {
  public ValidationException(String message) {
    super(message);
  }
}
