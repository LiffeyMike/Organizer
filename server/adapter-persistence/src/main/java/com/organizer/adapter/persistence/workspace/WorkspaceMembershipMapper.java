package com.organizer.adapter.persistence.workspace;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.organizer.coreconfig.id.MembershipId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.workspace.Role;
import com.organizer.domain.workspace.WorkspaceMembership;;

@Mapper(componentModel = "spring")
interface WorkspaceMembershipMapper {

  @Mapping(target = "id", expression = "java(new MembershipId(entity.getId()))")
  @Mapping(target = "workspaceId", expression = "java(new WorkspaceId(entity.getWorkspaceId()))")
  @Mapping(target = "userId", expression = "java(new UserId(entity.getUserId()))")
  @Mapping(target = "role", expression = "java(Role.valueOf(entity.getRole()))")
  WorkspaceMembership toDomain(JpaWorkspaceMembership entity);

  default JpaWorkspaceMembership toEntity(WorkspaceMembership membership) {
    return new JpaWorkspaceMembership(
        membership.id().id(), membership.workspaceId().id(), membership.userId().id(), membership.role().name(),
        membership.joinedAt());
  }
}
