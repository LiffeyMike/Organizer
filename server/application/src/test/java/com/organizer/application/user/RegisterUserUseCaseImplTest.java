package com.organizer.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.organizer.application.port.out.PasswordHasherPort;
import com.organizer.application.port.out.TokenIssuerPort;
import com.organizer.application.port.out.UserRepository;
import com.organizer.application.port.out.WorkspaceMembershipRepository;
import com.organizer.application.port.out.WorkspaceRepository;
import com.organizer.coreconfig.error.ConflictException;
import com.organizer.coreconfig.error.ValidationException;
import com.organizer.domain.auth.TokenPair;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.User;
import com.organizer.domain.workspace.Role;
import com.organizer.domain.workspace.Workspace;
import com.organizer.domain.workspace.WorkspaceMembership;
import com.organizer.domain.workspace.WorkspaceType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

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
    return new RegisterCommand("alice@wonder.land", PASSWORD, " ~Alice~ ");
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

    @Test
    void createsOnePersonalWorkspaceOwnedByTheNewUser() {
      givenRegistrationSucceeds();

      useCase.execute(validCommand());

      User user = savedUser();
      Workspace workspace = savedWorkspace();

      assertThat(workspace.type()).isEqualTo(WorkspaceType.PERSONAL);
      assertThat(workspace.createdBy()).isEqualTo(user.id());
      assertThat(workspace.name()).isEqualTo("~Alice~'s Organizer");
      assertThat(workspace.createdAt()).isEqualTo(NOW);
    }

    @Test
    void makesTheNewUserTHeOwnerOfThatWorkspace() {
      givenRegistrationSucceeds();

      useCase.execute(validCommand());

      User user = savedUser();
      Workspace workspace = savedWorkspace();
      WorkspaceMembership membership = savedMembership();

      assertThat(membership.userId()).isEqualTo(user.id());
      assertThat(membership.workspaceId()).isEqualTo(workspace.id());
      assertThat(membership.role()).isEqualTo(Role.OWNER);
      assertThat(membership.joinedAt()).isEqualTo(NOW);
    }

    @Test
    void issuesTokensForTheNewUserAndReturnsBoth() {
      givenRegistrationSucceeds();

      AuthResult result = useCase.execute(validCommand());

      User user = savedUser();

      verify(tokenIssuer).issueInitial(user.id(), user.email());
      assertThat(result.user()).isEqualTo(user);
      assertThat(result.tokens()).isEqualTo(TOKENS);
    }

    @Test
    void writesUserThenWorkspaceThenMembershipThenIssuesTokens() {
      givenRegistrationSucceeds();

      useCase.execute(validCommand());

      InOrder order =
          inOrder(userRepository, workspaceRepository, membershipRepository, tokenIssuer);
      order.verify(userRepository).save(any());
      order.verify(workspaceRepository).save(any());
      order.verify(membershipRepository).save(any());
      order.verify(tokenIssuer).issueInitial(any(), any());
    }
  }

  @Nested
  class Rejection {

    @Test
    void duplicateEmailIsAConflictExceptionAndNothingIsHashedOrWritten() {
      when(userRepository.existsByEmail(new Email("alice@wonder.land"))).thenReturn(true);

      assertThatThrownBy(() -> useCase.execute(validCommand()))
          .isInstanceOf(ConflictException.class)
          .hasMessage("An account with this email already exists");

      verify(userRepository).existsByEmail(any());
      verifyNoMoreInteractions(userRepository);
      verifyNoInteractions(passwordHasher, workspaceRepository, membershipRepository, tokenIssuer);
    }

    @Test
    void whenPasswordHasherRejectsNothingIsSaved() {
      ValidationException tooLong = new ValidationException("Password must be at most 72 bytes");
      when(userRepository.existsByEmail(any())).thenReturn(false);
      when(passwordHasher.hash(PASSWORD)).thenThrow(tooLong);

      assertThatThrownBy(() -> useCase.execute(validCommand()))
          .isInstanceOf(ValidationException.class);

      verify(userRepository, never()).save(any());
      verifyNoInteractions(workspaceRepository, membershipRepository, tokenIssuer);
    }

    @Test
    void malformedEmailIsRejectedBeforeAnyPortIsCalled() {
      assertThatThrownBy(
              () -> useCase.execute(new RegisterCommand("not-an-email", PASSWORD, "~alice~")))
          .isInstanceOf(ValidationException.class);

      verifyNoInteractions(
          userRepository, workspaceRepository, membershipRepository, passwordHasher, tokenIssuer);
    }

    @Test
    void blankDisplayNameIsRejectedAndNothingIsSaved() {
      when(userRepository.existsByEmail(any())).thenReturn(false);
      when(passwordHasher.hash(any())).thenReturn(HASH);

      assertThatThrownBy(
              () -> useCase.execute(new RegisterCommand("alice@wonder.land", PASSWORD, "   ")))
          .isInstanceOf(ValidationException.class);

      verify(userRepository, never()).save(any());
      verifyNoInteractions(workspaceRepository, membershipRepository, tokenIssuer);
    }

    @Test
    void executeRunsInOneTransaction() throws NoSuchMethodException {
      Transactional tx =
          RegisterUserUseCaseImpl.class
              .getMethod("execute", RegisterCommand.class)
              .getAnnotation(Transactional.class);

      assertThat(tx)
          .as("user, workspace, and membership must commit or rollback together")
          .isNotNull();
    }
  }
}
