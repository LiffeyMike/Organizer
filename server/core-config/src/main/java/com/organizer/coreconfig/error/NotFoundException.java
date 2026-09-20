package com.organizer.coreconfig.error;

public class NotFoundException extends DomainException {
  public NotFoundException(String message) {
    super(message);
  }
}
