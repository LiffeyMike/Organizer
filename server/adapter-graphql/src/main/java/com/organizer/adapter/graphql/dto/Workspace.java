package com.organizer.adapter.graphql.dto;

import com.organizer.adapter.graphql.id.GlobalId;
import com.organizer.adapter.graphql.node.Node;
import com.organizer.domain.workspace.WorkspaceType;

public record Workspace(String id, String name, WorkspaceType type) implements Node {

  public static final String TYPE = "Workspace";

  public static Workspace from(com.organizer.domain.workspace.Workspace workspace) {
    return new Workspace(
        GlobalId.encode(TYPE, workspace.id().id()), workspace.name(), workspace.type());
  }
}
