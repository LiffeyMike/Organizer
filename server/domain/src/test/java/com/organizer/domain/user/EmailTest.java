package com.organizer.domain.user;

import org.junit.jupiter.api.Test;

import com.organizer.coreconfig.error.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

  @Test
  void rejectsMalformedAddress() {
    assertThatThrownBy(() -> new Email("not.an.email"))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void normalizesCase() {
    Email email = new Email("Ada@LovElace.net");

    assertThat(email.value()).isEqualTo("ada@lovelace.net");
  }

  @Test
  void rejectsWhitespace() {
    assertThatThrownBy(() -> new Email("  ada@lovelace.net"))
        .isInstanceOf(ValidationException.class);
  }
}
