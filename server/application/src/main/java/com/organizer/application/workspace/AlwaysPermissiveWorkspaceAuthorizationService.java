package com.organizer.application.workspace;

import org.springframework.stereotype.Service;

import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;

@Service
public class AlwaysPermissiveWorkspaceAuthorizationService implements WorkspaceAuthorizationService {

  @Override
  public void requireMember(UserId userId, WorkspaceId workspaceId) {
    // noop
  }

  @Override
  public void requireOwner(UserId userId, WorkspaceId workspaceId) {
    // noop
  }
}
