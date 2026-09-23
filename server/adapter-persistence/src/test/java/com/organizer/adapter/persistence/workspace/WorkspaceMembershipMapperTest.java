package com.organizer.adapter.persistence.workspace;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.organizer.coreconfig.id.MembershipId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.workspace.WorkspaceMembership;
import com.organizer.domain.workspace.Role;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceMembershipMapperTest {

  private final WorkspaceMembershipMapper mapper = new WorkspaceMembershipMapper();

  @Test
  void roundTripsEveryField() {
    WorkspaceMembership membership = new WorkspaceMembership(
        new MembershipId(UUID.randomUUID()),
        new WorkspaceId(UUID.randomUUID()),
        new UserId(UUID.randomUUID()),
        Role.OWNER,
        Instant.parse("2026-01-01T00:00:00Z"));

    WorkspaceMembership roundTripped = mapper.toDomain(mapper.toEntity(membership));

    assertThat(roundTripped).isEqualTo(membership);
  }
}
