package com.organizer.application.user;

import com.organizer.application.port.out.WorkspaceRepository;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.page.Page;
import com.organizer.coreconfig.page.PageQuery;
import com.organizer.domain.workspace.Workspace;
import org.springframework.stereotype.Service;

@Service
class ListMyWorkspacesUseCaseImpl implements ListMyWorkspacesUseCase {

  private final WorkspaceRepository workspaceRepository;

  public ListMyWorkspacesUseCaseImpl(WorkspaceRepository workspaceRepository) {
    this.workspaceRepository = workspaceRepository;
  }

  @Override
  public Page<Workspace> execute(UserId userId, PageQuery pageQuery) {
    return workspaceRepository.findPageForUser(userId, pageQuery);
  }
}
