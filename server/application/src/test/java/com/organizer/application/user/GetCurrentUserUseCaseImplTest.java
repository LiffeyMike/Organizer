package com.organizer.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.organizer.application.port.out.UserRepository;
import com.organizer.coreconfig.error.NotFoundException;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserUseCaseImplTest {

  private static User RICHARD =
      new User(
          new UserId(UUID.randomUUID()),
          new Email("richard@de.clare"),
          new HashedPassword("$2a$12$strongbow"),
          "Strongbow",
          Instant.parse("2026-01-01T09:00:00Z"));

  @Mock UserRepository userRepository;

  private GetCurrentUserUseCaseImpl useCase;

  @BeforeEach
  void setup() {
    useCase = new GetCurrentUserUseCaseImpl(userRepository);
  }

  @Test
  void returnsTheUserTheIdNames() {
    when(userRepository.findById(RICHARD.id())).thenReturn(Optional.of(RICHARD));

    assertThat(useCase.execute(RICHARD.id())).isEqualTo(RICHARD);
  }

  @Test
  void userDeletedSinceTheTokenWasIssuedIsNotFound() {
    UserId deleted = new UserId(UUID.randomUUID());
    when(userRepository.findById(deleted)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(deleted))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("User not found");
  }
}
