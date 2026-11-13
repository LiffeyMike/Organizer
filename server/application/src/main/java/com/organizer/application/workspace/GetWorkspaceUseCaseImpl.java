package com.organizer.application.workspace;

import com.organizer.application.port.out.WorkspaceRepository;
import com.organizer.coreconfig.error.ForbiddenException;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.domain.workspace.Workspace;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GetWorkspaceUseCaseImpl implements GetWorkspaceUseCase {

  private final WorkspaceRepository workspaceRepository;
  private final WorkspaceAuthorizationService workspaceAuthorizationService;

  public GetWorkspaceUseCaseImpl(
      WorkspaceRepository workspaceRepository,
      WorkspaceAuthorizationService workspaceAuthorizationService) {
    this.workspaceRepository = workspaceRepository;
    this.workspaceAuthorizationService = workspaceAuthorizationService;
  }

  @Override
  public Optional<Workspace> execute(UserId callerId, WorkspaceId workspaceId) {
    try {
      workspaceAuthorizationService.requireMember(callerId, workspaceId);
    } catch (ForbiddenException e) {
      return Optional.empty();
    }

    return workspaceRepository.findById(workspaceId);
  }
}
