package com.organizer.adapter.security.password;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.organizer.coreconfig.error.ValidationException;
import com.organizer.domain.user.HashedPassword;
import org.junit.jupiter.api.Test;

class PasswordHasherTest {
  private static String RAW = "horseplay";

  private static final PasswordHasher hasher = new PasswordHasher();

  @Test
  void hasIsBcryptAtCost12() {
    HashedPassword hashed = hasher.hash(RAW);

    assertThat(hashed.value()).matches("^\\$2[aby]?\\$12\\$.{53}$").isNotEqualTo(RAW);
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
        .isInstanceOf(ValidationException.class)
        .hasMessage("Password must be at most 72 bytes");
  }

  @Test
  void limitCountsBytesNotCharacters() {
    String password = "\u00e9".repeat(37);
    assertThat(password).hasSize(37);

    assertThatThrownBy(() -> hasher.hash(password)).isInstanceOf(ValidationException.class);
  }

  @Test
  void maxLengthPasswordHashesAndMatches() {
    String rawPassword = "a".repeat(72);

    assertThat(hasher.matches(rawPassword, hasher.hash(rawPassword))).isTrue();
  }

  @Test
  void longerPasswordSharingTheFirst72BytesDoesNotMatch() {
    HashedPassword stored = hasher.hash("a".repeat(72));

    String tooLong = "a".repeat(72) + "b";

    assertThat(hasher.matches(tooLong, stored)).isFalse();
  }
}
