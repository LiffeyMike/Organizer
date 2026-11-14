package com.organizer.adapter.graphql.resolver;

import com.organizer.adapter.graphql.context.AuthContext;
import com.organizer.adapter.graphql.dto.Workspace;
import com.organizer.adapter.graphql.pagination.Connection;
import com.organizer.adapter.graphql.pagination.Connections;
import com.organizer.adapter.graphql.pagination.CursorPosition;
import com.organizer.adapter.security.AuthenticatedUser;
import com.organizer.application.workspace.ListMyWorkspacesUseCase;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.page.PageQuery;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WorkspaceController {

  private final ListMyWorkspacesUseCase listMyWorkspacesUseCase;

  public WorkspaceController(ListMyWorkspacesUseCase listMyWorkspacesUseCase) {
    this.listMyWorkspacesUseCase = listMyWorkspacesUseCase;
  }

  @QueryMapping
  public Connection<Workspace> myWorkspaces(
      @ContextValue(name = AuthContext.PRINCIPAL_KEY, required = false) AuthenticatedUser principal,
      @Argument Integer first,
      @Argument String after,
      @Argument Integer last,
      @Argument String before) {
    UserId userId = AuthContext.requireUserId(principal);
    PageQuery pageQuery = Connections.pageQuery(first, after, last, before);
    return Connections.of(
        listMyWorkspacesUseCase.execute(userId, pageQuery),
        workspace -> new CursorPosition(workspace.createdAt(), workspace.id().id()),
        Workspace::from);
  }
}
