package com.organizer.adapter.persistence.workspace;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.workspace.Workspace;
import com.organizer.domain.workspace.WorkspaceType;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceMapperTest {

  private final WorkspaceMapper mapper = new WorkspaceMapper();

  @Test
  void roundTripsEveryField() {
    Workspace workspace = new Workspace(
        new WorkspaceId(UUID.randomUUID()),
        "Mike's todo list",
        WorkspaceType.PERSONAL,
        new UserId(UUID.randomUUID()),
        Instant.parse("2026-01-01T00:00:00Z"));

    Workspace roundTripped = mapper.toDomain(mapper.toEntity(workspace));

    assertThat(roundTripped).isEqualTo(workspace);
  }
}
