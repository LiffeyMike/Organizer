package com.organizer.domain.workspace;

import com.organizer.coreconfig.id.MembershipId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import java.time.Instant;

public record WorkspaceMembership(
    MembershipId id, WorkspaceId workspaceId, UserId userId, Role role, Instant joinedAt) {

  public boolean canManageMembership() {
    return role == Role.OWNER;
  }

  public boolean canDeleteWorkspace() {
    return role == Role.OWNER;
  }
}
