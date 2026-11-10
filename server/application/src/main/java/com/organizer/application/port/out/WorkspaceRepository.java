package com.organizer.application.port.out;

import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.coreconfig.page.Page;
import com.organizer.coreconfig.page.PageQuery;
import com.organizer.domain.workspace.Workspace;
import java.util.Optional;

public interface WorkspaceRepository {
  Workspace save(Workspace workspace);

  Optional<Workspace> findById(WorkspaceId id);

  Page<Workspace> findPageForUser(UserId id, PageQuery pageQuery);
}
