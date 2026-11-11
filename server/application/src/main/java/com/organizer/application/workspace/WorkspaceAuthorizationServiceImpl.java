package com.organizer.application.workspace;

import com.organizer.application.port.out.WorkspaceMembershipRepository;
import com.organizer.coreconfig.error.ForbiddenException;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.domain.workspace.WorkspaceMembership;
import org.springframework.stereotype.Service;

@Service
class WorkspaceAuthorizationServiceImpl implements WorkspaceAuthorizationService {

  static final String NOT_A_MEMBER = "Not a member of this workspace";
  static final String NOT_AN_OWNER = "Only the workspace owner can perform this action";

  private final WorkspaceMembershipRepository membershipRepository;

  public WorkspaceAuthorizationServiceImpl(WorkspaceMembershipRepository membershipRepository) {
    this.membershipRepository = membershipRepository;
  }

  @Override
  public void requireMember(UserId userId, WorkspaceId workspaceId) {
    membershipOf(userId, workspaceId);
  }

  @Override
  public void requireOwner(UserId userId, WorkspaceId workspaceId) {
    if (!membershipOf(userId, workspaceId).canManageMembership()) {
      throw new ForbiddenException(NOT_AN_OWNER);
    }
  }

  private WorkspaceMembership membershipOf(UserId userId, WorkspaceId workspaceId) {
    return membershipRepository
        .findByWorkspaceAndUser(workspaceId, userId)
        .orElseThrow(() -> new ForbiddenException(NOT_A_MEMBER));
  }
}
