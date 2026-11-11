package com.organizer.application.user;

import com.organizer.application.port.out.PasswordHasherPort;
import com.organizer.application.port.out.TokenIssuerPort;
import com.organizer.application.port.out.UserRepository;
import com.organizer.application.port.out.WorkspaceMembershipRepository;
import com.organizer.application.port.out.WorkspaceRepository;
import com.organizer.coreconfig.error.ConflictException;
import com.organizer.coreconfig.id.MembershipId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.domain.auth.TokenPair;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.User;
import com.organizer.domain.workspace.Role;
import com.organizer.domain.workspace.Workspace;
import com.organizer.domain.workspace.WorkspaceMembership;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RegisterUserUseCaseImpl implements RegisterUserUseCase {

  private final UserRepository userRepository;
  private final WorkspaceRepository workspaceRepository;
  private final WorkspaceMembershipRepository membershipRepository;
  private final PasswordHasherPort passwordHasher;
  private final TokenIssuerPort tokenIssuer;
  private final Clock clock;

  public RegisterUserUseCaseImpl(
      UserRepository userRepository,
      WorkspaceRepository workspaceRepository,
      WorkspaceMembershipRepository membershipRepository,
      PasswordHasherPort passwordHasher,
      TokenIssuerPort tokenIssuer,
      Clock clock) {
    this.userRepository = userRepository;
    this.workspaceRepository = workspaceRepository;
    this.membershipRepository = membershipRepository;
    this.passwordHasher = passwordHasher;
    this.tokenIssuer = tokenIssuer;
    this.clock = clock;
  }

  @Override
  @Transactional
  public AuthResult execute(RegisterCommand command) {
    Email email = new Email(command.email());

    if (userRepository.existsByEmail(email)) {
      throw new ConflictException("An account with this email already exists");
    }

    HashedPassword hashed = passwordHasher.hash(command.rawPassword());
    User user =
        User.register(new UserId(UUID.randomUUID()), email, hashed, command.displayName(), clock);

    userRepository.save(user);

    Workspace workspace =
        Workspace.personalFor(
            new WorkspaceId(UUID.randomUUID()), user.id(), user.displayName(), clock);

    workspaceRepository.save(workspace);

    WorkspaceMembership membership =
        new WorkspaceMembership(
            new MembershipId(UUID.randomUUID()),
            workspace.id(),
            user.id(),
            Role.OWNER,
            clock.instant());

    membershipRepository.save(membership);

    TokenPair tokens = tokenIssuer.issueInitial(user.id(), user.email());
    return new AuthResult(user, tokens);
  }
}
