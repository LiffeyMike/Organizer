package com.organizer.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.organizer.application.port.out.UserRepository;
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
class GetUserUseCaseImplTest {

  private static final UserId CALLER_ID = new UserId(UUID.randomUUID());
  private static final User CALLER =
      new User(
          CALLER_ID,
          new Email("mike@admin.net"),
          new HashedPassword("password"),
          "Mikey mike",
          Instant.EPOCH);

  @Mock UserRepository userRepository;

  private GetUserUseCaseImpl useCase;

  @BeforeEach
  void setup() {
    this.useCase = new GetUserUseCaseImpl(userRepository);
  }

  @Test
  void callerCanSeeThemselves() {
    UserId sameCaller = new UserId(CALLER_ID.id());
    when(userRepository.findById(CALLER_ID)).thenReturn(Optional.of(CALLER));

    assertThat(useCase.execute(CALLER_ID, sameCaller)).contains(CALLER);
  }

  @Test
  void anotherUserIsInvisibleAndNeverLookedUp() {
    UserId anotherUser = new UserId(UUID.randomUUID());

    assertThat(useCase.execute(CALLER_ID, anotherUser)).isEmpty();
    verifyNoInteractions(userRepository);
  }

  @Test
  void callerWhoNoLongerExistsIsEmpty() {
    when(userRepository.findById(CALLER_ID)).thenReturn(Optional.empty());

    assertThat(useCase.execute(CALLER_ID, CALLER_ID)).isEmpty();
  }
}
