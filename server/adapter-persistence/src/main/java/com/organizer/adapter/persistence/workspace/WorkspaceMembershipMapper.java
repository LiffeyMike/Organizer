package com.organizer.adapter.persistence.workspace;

import org.springframework.stereotype.Component;

import com.organizer.coreconfig.id.MembershipId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.workspace.Role;
import com.organizer.domain.workspace.WorkspaceMembership;;

@Component
class WorkspaceMembershipMapper {

  WorkspaceMembership toDomain(JpaWorkspaceMembership entity) {
    return new WorkspaceMembership(
        new MembershipId(entity.getId()),
        new WorkspaceId(entity.getWorkspaceId()),
        new UserId(entity.getUserId()),
        Role.valueOf(entity.getRole()),
        entity.getJoinedAt());
  }

  JpaWorkspaceMembership toEntity(WorkspaceMembership membership) {
    JpaWorkspaceMembership entity = new JpaWorkspaceMembership();

    entity.setId(membership.id().id());
    entity.setWorkspaceId(membership.workspaceId().id());
    entity.setUserId(membership.userId().id());
    entity.setRole(membership.role().name());
    entity.setJoinedAt(membership.joinedAt());

    return entity;
  }
}
