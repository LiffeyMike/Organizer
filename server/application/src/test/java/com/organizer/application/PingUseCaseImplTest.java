package com.organizer.application;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PingUseCaseImplTest {

  @Test
  void returnsPong() {
    PingUseCase useCase = new PingUseCaseImpl();
    assertThat(useCase.ping()).isEqualTo("pong");
  }
}
