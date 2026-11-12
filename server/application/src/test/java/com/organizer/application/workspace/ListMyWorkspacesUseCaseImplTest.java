package com.organizer.application.workspace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.organizer.application.port.out.WorkspaceRepository;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.coreconfig.page.Page;
import com.organizer.coreconfig.page.PageQuery;
import com.organizer.domain.workspace.Workspace;
import com.organizer.domain.workspace.WorkspaceType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListMyWorkspacesUseCaseImplTest {

  @Mock WorkspaceRepository workspaceRepository;

  @Test
  void returnsTheRepositorysPageForTheCallerUnchanged() {
    UserId brian = new UserId(UUID.randomUUID());
    PageQuery pageQuery = PageQuery.firstPage(20);
    Workspace personal =
        new Workspace(
            new WorkspaceId(UUID.randomUUID()),
            "Brian's Organizer",
            WorkspaceType.PERSONAL,
            brian,
            Instant.parse("2026-01-01T09:00:00Z"));

    Page<Workspace> page = new Page<>(List.of(personal), true, false);
    when(workspaceRepository.findPageForUser(brian, pageQuery)).thenReturn(page);

    Page<Workspace> result =
        new ListMyWorkspacesUseCaseImpl(workspaceRepository).execute(brian, pageQuery);

    assertThat(result).isSameAs(page);
  }
}
