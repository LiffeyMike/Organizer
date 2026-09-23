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
    JpaWorkspace entity = new JpaWorkspace();

    entity.setId(workspace.id().id());
    entity.setName(workspace.name());
    entity.setType(workspace.type().name());
    entity.setCreatedBy(workspace.createdBy().id());
    entity.setCreatedAt(workspace.createdAt());

    return entity;
  }
}
