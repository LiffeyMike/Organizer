package com.organizer.application.workspace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.organizer.application.port.out.WorkspaceRepository;
import com.organizer.coreconfig.error.ForbiddenException;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.domain.workspace.Workspace;
import com.organizer.domain.workspace.WorkspaceType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetWorkspaceUseCaseImplTest {

  private static final UserId CALLER = new UserId(UUID.randomUUID());
  private static final WorkspaceId WORKSPACE_ID = new WorkspaceId(UUID.randomUUID());

  @Mock WorkspaceAuthorizationService authorization;
  @Mock WorkspaceRepository workspaceRepository;

  private GetWorkspaceUseCaseImpl useCase;

  @BeforeEach
  void setup() {
    useCase = new GetWorkspaceUseCaseImpl(workspaceRepository, authorization);
  }

  @Test
  void memberGetsTheWorkspaceAfterTheMembershipCheck() {
    Workspace workspace =
        new Workspace(WORKSPACE_ID, "Team", WorkspaceType.SHARED, CALLER, Instant.EPOCH);

    when(workspaceRepository.findById(WORKSPACE_ID)).thenReturn(Optional.of(workspace));

    assertThat(useCase.execute(CALLER, WORKSPACE_ID)).contains(workspace);

    InOrder order = inOrder(authorization, workspaceRepository);
    order.verify(authorization).requireMember(CALLER, WORKSPACE_ID);
    order.verify(workspaceRepository).findById(WORKSPACE_ID);
  }

  @Test
  void nonMemberGetsNothingAndTheWorkspaceIsNeverRead() {
    doThrow(new ForbiddenException("Not a member of this workspace"))
        .when(authorization)
        .requireMember(CALLER, WORKSPACE_ID);

    assertThat(useCase.execute(CALLER, WORKSPACE_ID)).isEmpty();
    verifyNoInteractions(workspaceRepository);
  }
}
