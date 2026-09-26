package com.organizer.adapter.persistence.workspace;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.organizer.adapter.persistence.AbstractIntegrationTest;
import com.organizer.adapter.persistence.user.UserRepositoryAdapter;
import com.organizer.coreconfig.id.MembershipId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.User;
import com.organizer.domain.workspace.Role;
import com.organizer.domain.workspace.Workspace;
import com.organizer.domain.workspace.WorkspaceMembership;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceMembershipRepositoryAdapterTest extends AbstractIntegrationTest {

  private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

  @Autowired
  UserRepositoryAdapter users;

  @Autowired
  WorkspaceRepositoryAdapter workspaces;

  @Autowired
  WorkspaceMembershipRepositoryAdapter memberships;

  @Test
  void savesAndFindsByWorkspaceAndUser() {
    User owner = users.save(User.register(
        new UserId(UUID.randomUUID()), new Email("a@b.com"), new HashedPassword("hash"), "Ada", fixedClock));
    Workspace workspace = workspaces.save(Workspace.personalFor(
        new WorkspaceId(UUID.randomUUID()), owner.id(), "Ada", fixedClock));

    WorkspaceMembership membership = memberships.save(new WorkspaceMembership(
        new MembershipId(UUID.randomUUID()), workspace.id(), owner.id(), Role.OWNER, fixedClock.instant()));

    assertThat(memberships.findByWorkspaceAndUser(workspace.id(), owner.id())).contains(membership);
  }

  @Test
  void absentMembershipReturnsEmptyNotException() {
    Optional<WorkspaceMembership> result = memberships.findByWorkspaceAndUser(
        new WorkspaceId(UUID.randomUUID()), new UserId(UUID.randomUUID()));

    assertThat(result).isEmpty();
  }

}
