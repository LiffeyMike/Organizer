package com.organizer.domain.user;

import java.util.regex.Pattern;

import com.organizer.coreconfig.error.ValidationException;

public record Email(String value) {

  private static final Pattern FORMAT = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

  public Email {
    if (value == null || !FORMAT.matcher(value).matches()) {
      throw new ValidationException("Malformed email address");
    }

    value = value.trim().toLowerCase();
  }

}
