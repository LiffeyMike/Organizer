package com.organizer.coreconfig.error;

public class ForbiddenException extends DomainException {
  public ForbiddenException(String message) {
    super(message);
  }
}
