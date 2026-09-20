package com.organizer.adapter.security.password;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {
  private final PasswordEncoder encoder = new BCryptPasswordEncoder(12);

  public String hash(String rawPassword) {
    return encoder.encode(rawPassword);
  }

  public Boolean matches(String rawPassword, String hash) {
    return encoder.matches(rawPassword, hash);
  }
}
