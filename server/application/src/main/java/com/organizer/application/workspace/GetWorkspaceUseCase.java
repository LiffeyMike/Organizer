package com.organizer.application.workspace;

import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.domain.workspace.Workspace;
import java.util.Optional;

public interface GetWorkspaceUseCase {

  Optional<Workspace> execute(UserId callerId, WorkspaceId workspaceId);
}
