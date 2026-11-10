package com.organizer.application.port.out;

import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.domain.workspace.WorkspaceMembership;
import java.util.Optional;

public interface WorkspaceMembershipRepository {

  WorkspaceMembership save(WorkspaceMembership membership);

  Optional<WorkspaceMembership> findByWorkspaceAndUser(WorkspaceId workspaceId, UserId userId);
}
