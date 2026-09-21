package com.organizer.domain.workspace;

import java.time.Clock;
import java.time.Instant;

import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;

public record Workspace(WorkspaceId id, String name, WorkspaceType type, UserId createdBy, Instant createdAt) {
  public static Workspace personalFor(WorkspaceId id, UserId owner, String ownerDisplayName, Clock clock) {
    String worksaceName = ownerDisplayName + "'s Organizer";
    return new Workspace(id, worksaceName, WorkspaceType.PERSONAL, owner, clock.instant());
  }
}
