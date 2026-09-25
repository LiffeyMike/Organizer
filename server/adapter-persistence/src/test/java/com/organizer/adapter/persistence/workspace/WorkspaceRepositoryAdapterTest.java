package com.organizer.adapter.persistence.workspace;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.organizer.adapter.persistence.AbstractIntegrationTest;
import com.organizer.adapter.persistence.user.UserRepositoryAdapter;
import com.organizer.coreconfig.id.MembershipId;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.coreconfig.page.Page;
import com.organizer.coreconfig.page.PageQuery;
import com.organizer.domain.user.User;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.workspace.Workspace;
import com.organizer.domain.workspace.Role;
import com.organizer.domain.workspace.WorkspaceMembership;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkspaceRepositoryAdapterTest extends AbstractIntegrationTest {

  @Autowired
  UserRepositoryAdapter users;

  @Autowired
  WorkspaceRepositoryAdapter workspaces;

  @Autowired
  WorkspaceMembershipRepositoryAdapter memberships;

  private User owner;

  @BeforeEach
  void createOwner() {
    Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
    User owner = User.register(
        new UserId(UUID.randomUUID()),
        new Email("admin@computer.net"),
        new HashedPassword("h4sh3ed"),
        "Admin",
        fixedClock);
    this.owner = users.save(owner);
  }

  @Test
  void savesAndFindsById() {
    Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
    Workspace workspace = Workspace.personalFor(
        new WorkspaceId(UUID.randomUUID()), owner.id(), owner.displayName(), fixedClock);
    workspaces.save(workspace);

    assertThat(workspaces.findById(workspace.id())).contains(workspace);
  }

  @Test
  void findPageForUserOnlyReturnsWorkspacesUserBelongsToInCreatedOrder() {
    Workspace earlier = saveWorkspaceWithMembership(Instant.parse("2026-01-01T00:00:00Z"));
    Workspace later = saveWorkspaceWithMembership(Instant.parse("2026-01-02T00:00:00Z"));

    // This workspace is saved, but no membership is added for the owner.
    // It should not be returned for the User's workspaces
    workspaces.save(Workspace.personalFor(
        new WorkspaceId(UUID.randomUUID()),
        owner.id(),
        owner.displayName(),
        Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)));

    Page<Workspace> page = workspaces.findPageForUser(owner.id(), PageQuery.firstPage(10));

    assertThat(page.items()).containsExactly(earlier, later);
    assertThat(page.hasNextPage()).isFalse();
    assertThat(page.hasPreviousPage()).isFalse();
  }

  @Test
  void findPageForUserHonoursLimitAndReportsHasNextPage() {
    Workspace earlier = saveWorkspaceWithMembership(Instant.parse("2026-01-01T00:00:00Z"));
    saveWorkspaceWithMembership(Instant.parse("2026-01-02T00:00:00Z"));

    Page<Workspace> page = workspaces.findPageForUser(owner.id(), PageQuery.firstPage(1));
    assertThat(page.items()).hasSize(1);
    assertThat(page.hasNextPage()).isTrue();
    assertThat(page.items().getFirst()).isEqualTo(earlier);
    assertThat(page.hasPreviousPage()).isFalse();
  }

  private Workspace saveWorkspaceWithMembership(Instant createdAt) {
    Clock fixedClock = Clock.fixed(createdAt, ZoneOffset.UTC);
    Workspace workspace = Workspace.personalFor(
        new WorkspaceId(UUID.randomUUID()),
        owner.id(),
        owner.displayName(),
        fixedClock);

    Workspace saved = workspaces.save(workspace);
    memberships.save(
        new WorkspaceMembership(new MembershipId(UUID.randomUUID()),
            saved.id(),
            owner.id(),
            Role.OWNER,
            fixedClock.instant()));

    return saved;
  }

}
