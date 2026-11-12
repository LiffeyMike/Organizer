package com.organizer.application.workspace;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.organizer.application.port.out.WorkspaceMembershipRepository;
import com.organizer.coreconfig.error.ForbiddenException;
import com.organizer.coreconfig.id.MembershipId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.domain.workspace.Role;
import com.organizer.domain.workspace.WorkspaceMembership;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkspaceAuthorizationServiceImplTest {

  private static final UserId NEO = new UserId(UUID.randomUUID());
  private static final WorkspaceId WORKSPACE = new WorkspaceId(UUID.randomUUID());

  @Mock WorkspaceMembershipRepository membershipRepository;

  private WorkspaceAuthorizationServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new WorkspaceAuthorizationServiceImpl(membershipRepository);
  }

  private void givenNeoIs(Role role) {
    WorkspaceMembership membership =
        new WorkspaceMembership(
            new MembershipId(UUID.randomUUID()), WORKSPACE, NEO, role, Instant.EPOCH);

    when(membershipRepository.findByWorkspaceAndUser(WORKSPACE, NEO))
        .thenReturn(Optional.of(membership));
  }

  private void givenNeoIsNotAMember() {
    when(membershipRepository.findByWorkspaceAndUser(WORKSPACE, NEO)).thenReturn(Optional.empty());
  }

  @Nested
  class RequireMember {

    @ParameterizedTest
    @EnumSource(Role.class)
    void passesForAnyRole(Role role) {
      givenNeoIs(role);

      assertThatCode(() -> service.requireMember(NEO, WORKSPACE)).doesNotThrowAnyException();
    }

    @Test
    void nonMemberIsForbidden() {
      givenNeoIsNotAMember();

      assertThatThrownBy(() -> service.requireMember(NEO, WORKSPACE))
          .isInstanceOf(ForbiddenException.class)
          .hasMessage(WorkspaceAuthorizationServiceImpl.NOT_A_MEMBER);
    }
  }

  @Nested
  class RequireOwner {

    @Test
    void passesForOwner() {
      givenNeoIs(Role.OWNER);

      assertThatCode(() -> service.requireOwner(NEO, WORKSPACE)).doesNotThrowAnyException();
    }

    @Test
    void plainMemberIsForbidden() {
      givenNeoIs(Role.MEMBER);

      assertThatThrownBy(() -> service.requireOwner(NEO, WORKSPACE))
          .isInstanceOf(ForbiddenException.class)
          .hasMessage(WorkspaceAuthorizationServiceImpl.NOT_THE_OWNER);
    }

    @Test
    void nonMemberIsForbidden() {
      givenNeoIsNotAMember();

      assertThatThrownBy(() -> service.requireOwner(NEO, WORKSPACE))
          .isInstanceOf(ForbiddenException.class)
          .hasMessage(WorkspaceAuthorizationServiceImpl.NOT_A_MEMBER);
    }
  }
}
