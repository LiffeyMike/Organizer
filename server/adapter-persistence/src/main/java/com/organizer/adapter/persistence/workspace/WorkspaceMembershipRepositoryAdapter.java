package com.organizer.adapter.persistence.workspace;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.organizer.application.port.out.WorkspaceMembershipRepository;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.domain.workspace.WorkspaceMembership;

@Component
public class WorkspaceMembershipRepositoryAdapter implements WorkspaceMembershipRepository {

  private final JpaWorkspaceMembershipRepository jpaRepository;
  private final WorkspaceMembershipMapper mapper;

  public WorkspaceMembershipRepositoryAdapter(JpaWorkspaceMembershipRepository workspaceMembershipRepository,
      WorkspaceMembershipMapper mapper) {
    this.jpaRepository = workspaceMembershipRepository;
    this.mapper = mapper;
  }

  @Override
  public WorkspaceMembership save(WorkspaceMembership membership) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(membership)));
  }

  @Override
  public Optional<WorkspaceMembership> findByWorkspaceAndUser(WorkspaceId workspaceId, UserId userId) {
    return jpaRepository.findByWorkspaceIdAndUserId(workspaceId.id(), userId.id()).map(mapper::toDomain);
  }
}
