package com.organizer.adapter.security.password;

import org.junit.jupiter.api.Test;

import com.organizer.domain.user.HashedPassword;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;;

class PasswordHasherTest {
  private static String RAW = "horseplay";

  private static final PasswordHasher hasher = new PasswordHasher();

  @Test
  void hasIsBcryptAtCost12() {
    HashedPassword hashed = hasher.hash(RAW);

    assertThat(hashed.value())
        .matches("^\\$2[aby]?\\$12\\$.{53}$")
        .isNotEqualTo(RAW);
  }

  @Test
  void correctPasswordMatches() {
    HashedPassword hashed = hasher.hash(RAW);

    assertThat(hasher.matches(RAW, hashed)).isTrue();
  }

  @Test
  void wrongPasswordDoesNotMatch() {
    HashedPassword hashed = hasher.hash(RAW);

    assertThat(hasher.matches("wrongpassword", hashed)).isFalse();
    assertThat(hasher.matches("something else", hashed)).isFalse();
  }

  @Test
  void hashesAreSalted() {
    HashedPassword first = hasher.hash(RAW);
    HashedPassword second = hasher.hash(RAW);

    assertThat(first).isNotEqualTo(second);
    assertThat(hasher.matches(RAW, first)).isTrue();
    assertThat(hasher.matches(RAW, second)).isTrue();
  }

  @Test
  void malformedStoredHashFailsClosed() {
    assertThat(hasher.matches(RAW, new HashedPassword("not a bcrypt hash"))).isFalse();
  }

  @Test
  void passwordsOver72BytesAreRejected() {
    String tooLong = "a".repeat(73);

    assertThatThrownBy(() -> hasher.hash(tooLong))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
