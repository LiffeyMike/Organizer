package com.organizer.adapter.security.password;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.organizer.application.port.out.PasswordHasherPort;
import com.organizer.domain.user.HashedPassword;

@Component
public class PasswordHasher implements PasswordHasherPort {
  private final PasswordEncoder encoder = new BCryptPasswordEncoder(12);

  @Override
  public HashedPassword hash(String rawPassword) {
    return new HashedPassword(encoder.encode(rawPassword));
  }

  @Override
  public boolean matches(String rawPassword, HashedPassword hash) {
    return encoder.matches(rawPassword, hash.value());
  }
}
