package com.organizer.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PingUseCaseImplTest {

  @Test
  void returnsPong() {
    PingUseCase useCase = new PingUseCaseImpl();
    assertThat(useCase.ping()).isEqualTo("pong");
  }
}
