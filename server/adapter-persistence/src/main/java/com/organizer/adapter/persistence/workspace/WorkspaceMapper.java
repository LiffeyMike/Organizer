package com.organizer.adapter.persistence.workspace;

import org.springframework.stereotype.Component;

import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.domain.workspace.Workspace;
import com.organizer.domain.workspace.WorkspaceType;

@Component
class WorkspaceMapper {

  Workspace toDomain(JpaWorkspace entity) {
    return new Workspace(
        new WorkspaceId(entity.getId()),
        entity.getName(),
        WorkspaceType.valueOf(entity.getType()),
        new UserId(entity.getCreatedBy()),
        entity.getCreatedAt());
  }

  JpaWorkspace toEntity(Workspace workspace) {
    JpaWorkspace entity = new JpaWorkspace();

    entity.setId(workspace.id().id());
    entity.setName(workspace.name());
    entity.setType(workspace.type().name());
    entity.setCreatedBy(workspace.createdBy().id());
    entity.setCreatedAt(workspace.createdAt());

    return entity;
  }
}
