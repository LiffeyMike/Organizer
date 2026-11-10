package com.organizer.domain.workspace;

import static org.assertj.core.api.Assertions.assertThat;

import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WorkspaceTest {

  @Test
  void personalForNamesWorkspaceAfterOwner() {
    Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
    UserId owner = new UserId(UUID.randomUUID());
    String ownerDisplayName = "Ada Lovelace";

    Workspace workspace =
        Workspace.personalFor(
            new WorkspaceId(UUID.randomUUID()), owner, ownerDisplayName, fixedClock);

    assertThat(workspace.name()).isEqualTo("Ada Lovelace's Organizer");
    assertThat(workspace.type()).isEqualTo(WorkspaceType.PERSONAL);
    assertThat(workspace.createdBy()).isEqualTo(owner);
  }
}
