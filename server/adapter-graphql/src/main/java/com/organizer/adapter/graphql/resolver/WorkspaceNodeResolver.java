package com.organizer.adapter.graphql.resolver;

import com.organizer.adapter.graphql.dto.Workspace;
import com.organizer.adapter.graphql.node.Node;
import com.organizer.adapter.graphql.node.NodeResolver;
import com.organizer.application.workspace.GetWorkspaceUseCase;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceNodeResolver implements NodeResolver {

  private final GetWorkspaceUseCase getWorkspaceUseCase;

  public WorkspaceNodeResolver(GetWorkspaceUseCase getWorkspaceUseCase) {
    this.getWorkspaceUseCase = getWorkspaceUseCase;
  }

  @Override
  public String typeName() {
    return Workspace.TYPE;
  }

  @Override
  public Optional<Node> resolve(UserId callerId, UUID workspaceId) {
    return getWorkspaceUseCase.execute(callerId, new WorkspaceId(workspaceId)).map(Workspace::from);
  }
}
