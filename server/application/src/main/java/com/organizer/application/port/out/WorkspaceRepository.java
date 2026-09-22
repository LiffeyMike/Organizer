package com.organizer.application.port.out;

import java.util.Optional;

import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.coreconfig.page.Page;
import com.organizer.coreconfig.page.PageQuery;
import com.organizer.domain.workspace.Workspace;

public interface WorkspaceRepository {
  Workspace save(Workspace workspace);

  Optional<Workspace> findById(WorkspaceId id);

  Page<Workspace> findPageForUser(UserId id, PageQuery pageQuery);

};
