package com.organizer.application.workspace;

import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;

public interface WorkspaceAuthorizationService {
  void requireMember(UserId userId, WorkspaceId workspaceId);

  void requireOwner(UserId userId, WorkspaceId workspaceId);
}
