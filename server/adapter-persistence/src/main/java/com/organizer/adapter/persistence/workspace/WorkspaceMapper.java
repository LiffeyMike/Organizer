package com.organizer.adapter.persistence.workspace;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.domain.workspace.Workspace;
import com.organizer.domain.workspace.WorkspaceType;

@Mapper(componentModel = "spring")
interface WorkspaceMapper {

  @Mapping(target = "id", expression = "java(new WorkspaceId(entity.getId()))")
  @Mapping(target = "type", expression = "java(WorkspaceType.valueOf(entity.getType()))")
  @Mapping(target = "createdBy", expression = "java(new UserId(entity.getCreatedBy()))")
  Workspace toDomain(JpaWorkspace entity);

  default JpaWorkspace toEntity(Workspace workspace) {
    return new JpaWorkspace(
        workspace.id().id(), workspace.name(), workspace.type().name(), workspace.createdBy().id(),
        workspace.createdAt());
  }
}
