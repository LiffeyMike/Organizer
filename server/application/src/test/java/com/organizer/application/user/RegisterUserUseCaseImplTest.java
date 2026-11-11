package com.organizer.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.organizer.application.port.out.PasswordHasherPort;
import com.organizer.application.port.out.TokenIssuerPort;
import com.organizer.application.port.out.UserRepository;
import com.organizer.application.port.out.WorkspaceMembershipRepository;
import com.organizer.application.port.out.WorkspaceRepository;
import com.organizer.domain.auth.TokenPair;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.User;
import com.organizer.domain.workspace.Workspace;
import com.organizer.domain.workspace.WorkspaceMembership;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseImplTest {

  private static final Instant NOW = Instant.parse("2026-01-01T09:00:00Z");
  private static final String PASSWORD = "correctly painted porch";
  private static final HashedPassword HASH = new HashedPassword("$2a$12$stubbed-hash");
  private static final TokenPair TOKENS = new TokenPair("access-token", "refresh-token");

  @Mock UserRepository userRepository;
  @Mock WorkspaceRepository workspaceRepository;
  @Mock WorkspaceMembershipRepository membershipRepository;
  @Mock PasswordHasherPort passwordHasher;
  @Mock TokenIssuerPort tokenIssuer;

  private RegisterUserUseCaseImpl useCase;

  @BeforeEach
  void setup() {
    useCase =
        new RegisterUserUseCaseImpl(
            userRepository,
            workspaceRepository,
            membershipRepository,
            passwordHasher,
            tokenIssuer,
            Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private static RegisterCommand validCommand() {
    return new RegisterCommand("Alice@wonder.land", PASSWORD, " ~Alice~ ");
  }

  private void givenRegistrationSucceeds() {
    when(userRepository.existsByEmail(new Email("alice@wonder.land"))).thenReturn(false);
    when(passwordHasher.hash(any())).thenReturn(HASH);
    when(userRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    when(workspaceRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    when(membershipRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    when(tokenIssuer.issueInitial(any(), any())).thenReturn(TOKENS);
  }

  private User savedUser() {
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    return captor.getValue();
  }

  private Workspace savedWorkspace() {
    ArgumentCaptor<Workspace> captor = ArgumentCaptor.forClass(Workspace.class);
    verify(workspaceRepository).save(captor.capture());
    return captor.getValue();
  }

  private WorkspaceMembership savedMembership() {
    ArgumentCaptor<WorkspaceMembership> captor = ArgumentCaptor.forClass(WorkspaceMembership.class);
    verify(membershipRepository).save(captor.capture());
    return captor.getValue();
  }

  @Nested
  class Success {

    @Test
    void savesUserWithNormalisedEmailHashedPasswordAndTrimmedName() {
      givenRegistrationSucceeds();

      useCase.execute(validCommand());

      verify(passwordHasher).hash(PASSWORD);
      User user = savedUser();

      assertThat(user.email().value()).isEqualTo("alice@wonder.land");
      assertThat(user.password()).isEqualTo(HASH);
      assertThat(user.displayName()).isEqualTo("~Alice~");
      assertThat(user.createdAt()).isEqualTo(NOW);
    }
  }
}
