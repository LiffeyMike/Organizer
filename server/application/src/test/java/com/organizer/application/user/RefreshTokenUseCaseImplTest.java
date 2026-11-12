package com.organizer.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.organizer.application.port.out.TokenIssuerPort;
import com.organizer.application.port.out.UserRepository;
import com.organizer.coreconfig.error.InvalidCredentialsException;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.auth.TokenPair;
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
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseImplTest {

  private static final User LEON =
      new User(
          new UserId(UUID.randomUUID()),
          new Email("leon@star.gov"),
          new HashedPassword("$2a$12$racoon"),
          "Leon",
          Instant.parse("2026-01-01T09:00:00Z"));

  private static final String PRESENTED = "presented-refresh-token";
  private static final TokenPair ROTATED = new TokenPair("access-token", "refresh-token");

  @Mock TokenIssuerPort tokenIssuer;
  @Mock UserRepository userRepository;

  private RefreshTokenUseCaseImpl useCase;

  @BeforeEach
  void setup() {
    useCase = new RefreshTokenUseCaseImpl(tokenIssuer, userRepository);
  }

  @Test
  void returnsTheRotatedPairWithTheUserItBelongsTo() {
    when(tokenIssuer.rotateRefreshToken(PRESENTED)).thenReturn(ROTATED);
    when(tokenIssuer.parseAccessToken(ROTATED.accessToken())).thenReturn(Optional.of(LEON.id()));
    when(userRepository.findById(LEON.id())).thenReturn(Optional.of(LEON));

    AuthResult result = useCase.execute(PRESENTED);

    assertThat(result.tokens()).isEqualTo(ROTATED);
    assertThat(result.user()).isEqualTo(LEON);
  }

  @Test
  void rejectedRefreshTokenPropogatesUntouched() {
    InvalidCredentialsException rejection =
        new InvalidCredentialsException("Invalid refresh token");
    when(tokenIssuer.rotateRefreshToken(PRESENTED)).thenThrow(rejection);

    assertThatThrownBy(() -> useCase.execute(PRESENTED))
        .isInstanceOf(rejection.getClass())
        .hasMessage("Invalid refresh token");

    verify(tokenIssuer, never()).parseAccessToken(any());
    verifyNoInteractions(userRepository);
  }

  @Test
  void unparseableFreshAccessTokenIsABugNotBadCredentials() {
    when(tokenIssuer.rotateRefreshToken(PRESENTED)).thenReturn(ROTATED);
    when(tokenIssuer.parseAccessToken(ROTATED.accessToken())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(PRESENTED)).isInstanceOf(IllegalStateException.class);
    verifyNoInteractions(userRepository);
  }

  @Test
  void executeIsTransactionalAndKeepsRevocationsOnInvalidCredentials()
      throws NoSuchMethodException {
    Transactional tx =
        RefreshTokenUseCaseImpl.class
            .getMethod("execute", String.class)
            .getAnnotation(Transactional.class);

    assertThat(tx).as("rotation's two writes must be atomic").isNotNull();
    assertThat(tx.noRollbackFor())
        .as("a replay's family revocation must commit even though execute throws")
        .containsExactly(InvalidCredentialsException.class);
  }
}
