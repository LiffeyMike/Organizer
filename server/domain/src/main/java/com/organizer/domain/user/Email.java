package com.organizer.domain.user;

import com.organizer.coreconfig.error.ValidationException;
import java.util.regex.Pattern;

public record Email(String value) {

  private static final Pattern FORMAT = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

  public Email {
    if (value == null || !FORMAT.matcher(value).matches()) {
      throw new ValidationException("Malformed email address");
    }

    value = value.trim().toLowerCase();
  }
}
