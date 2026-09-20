package com.organizer.coreconfig.error;

public class UnauthenticatedException extends DomainException {
  public UnauthenticatedException(String message) {
    super(message);
  }
}
