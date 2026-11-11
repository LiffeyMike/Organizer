package com.organizer.adapter.security.password;

import com.organizer.application.port.out.PasswordHasherPort;
import com.organizer.coreconfig.error.ValidationException;
import com.organizer.domain.user.HashedPassword;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher implements PasswordHasherPort {

  static final int MAX_BYTES = 72;

  private final PasswordEncoder encoder = new BCryptPasswordEncoder(12);

  @Override
  public HashedPassword hash(String rawPassword) {
    if (!fits(rawPassword)) {
      throw new ValidationException("Password must be at most " + MAX_BYTES + " bytes");
    }

    return new HashedPassword(encoder.encode(rawPassword));
  }

  @Override
  public boolean matches(String rawPassword, HashedPassword hash) {
    return fits(rawPassword) && encoder.matches(rawPassword, hash.value());
  }

  private static boolean fits(String rawPassword) {
    return rawPassword != null && rawPassword.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES;
  }
}
