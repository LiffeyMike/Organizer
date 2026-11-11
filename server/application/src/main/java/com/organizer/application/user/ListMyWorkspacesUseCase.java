package com.organizer.application.user;

import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.page.Page;
import com.organizer.coreconfig.page.PageQuery;
import com.organizer.domain.workspace.Workspace;

public interface ListMyWorkspacesUseCase {

  Page<Workspace> execute(UserId userId, PageQuery pageQuery);
}
