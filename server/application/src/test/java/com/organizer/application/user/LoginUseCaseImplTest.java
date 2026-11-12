package com.organizer.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.organizer.application.port.out.PasswordHasherPort;
import com.organizer.application.port.out.TokenIssuerPort;
import com.organizer.application.port.out.UserRepository;
import com.organizer.coreconfig.error.InvalidCredentialsException;
import com.organizer.coreconfig.error.ValidationException;
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

@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

  private static final HashedPassword ADA_HASH = new HashedPassword("$2a$12$ADA");
  private static final HashedPassword DECOY_HASH = new HashedPassword("$2a$12$DECOY");
  private static final User ADA =
      new User(
          new UserId(UUID.fromString("00000000-0000-0000-0000-00000000a11c")),
          new Email("ada@comp.sci"),
          ADA_HASH,
          "Lovely Ada",
          Instant.parse("2026-01-01T09:00:00Z"));
  private static final Email NOBODY = new Email("nobody@nsa.gov");
  private static final TokenPair TOKENS = new TokenPair("access-token", "refresh-token");

  @Mock UserRepository userRepository;
  @Mock PasswordHasherPort passwordHasher;
  @Mock TokenIssuerPort tokenIssuer;

  private LoginUseCaseImpl useCase;

  @BeforeEach
  void setup() {
    when(passwordHasher.hash(anyString())).thenReturn(DECOY_HASH);
    useCase = new LoginUseCaseImpl(userRepository, passwordHasher, tokenIssuer);
  }

  private AuthResult login(String email, String password) {
    return useCase.execute(new LoginCommand(email, password));
  }

  @Test
  void correctPasswordStartsANewSessionForThatUser() {
    when(userRepository.findByEmail(ADA.email())).thenReturn(Optional.of(ADA));
    when(passwordHasher.matches("right", ADA_HASH)).thenReturn(true);
    when(tokenIssuer.issueInitial(ADA.id(), ADA.email())).thenReturn(TOKENS);

    AuthResult result = login("ada@comp.sci", "right");

    assertThat(result.user()).isEqualTo(ADA);
    assertThat(result.tokens()).isEqualTo(TOKENS);
    verify(tokenIssuer, never()).rotateRefreshToken(any());
  }

  @Test
  void wrongPasswordIsInvalidCredentials() {
    when(userRepository.findByEmail(ADA.email())).thenReturn(Optional.of(ADA));
    when(passwordHasher.matches("wrong", ADA_HASH)).thenReturn(false);

    assertThatThrownBy(() -> login("ada@comp.sci", "wrong"))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage(LoginUseCaseImpl.INVALID_CREDENTIALS);

    verifyNoInteractions(tokenIssuer);
  }

  @Test
  void unknownEmailIsInvalidCredentials() {
    when(userRepository.findByEmail(NOBODY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> login(NOBODY.value(), "anything"))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage(LoginUseCaseImpl.INVALID_CREDENTIALS);

    verifyNoInteractions(tokenIssuer);
  }

  @Test
  void unknownEmailAndWrongPasswordAreIndistinguishable() {
    when(userRepository.findByEmail(ADA.email())).thenReturn(Optional.of(ADA));
    when(userRepository.findByEmail(NOBODY)).thenReturn(Optional.empty());

    Throwable wrongPassword = catchThrowable(() -> login("ada@comp.sci", "wrong"));
    Throwable unknownEmail = catchThrowable(() -> login(NOBODY.value(), "wrong"));

    assertThat(wrongPassword)
        .isExactlyInstanceOf(unknownEmail.getClass())
        .hasMessage(unknownEmail.getMessage());
  }

  @Test
  void unknownEmailStillRunsAPasswordCheck() {
    when(userRepository.findByEmail(NOBODY)).thenReturn(Optional.empty());

    catchThrowable(() -> login(NOBODY.value(), "guess"));

    verify(passwordHasher).matches("guess", DECOY_HASH);
  }

  @Test
  void malformedEmailIsAValidationExceptionNotInvalidCredentials() {
    assertThatThrownBy(() -> login("not-an-email", "anything"))
        .isInstanceOf(ValidationException.class);

    verifyNoInteractions(userRepository, tokenIssuer);
    verify(passwordHasher, never()).matches(any(), any());
  }
}
