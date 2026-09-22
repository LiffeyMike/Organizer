package com.organizer.adapter.persistence.workspace;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "workspace_memberships")
public class JpaWorkspaceMembership {

  @Id
  private UUID id;

  @Column(name = "workspace_id", nullable = false)
  private UUID workspaceId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private String role;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt;

  protected JpaWorkspaceMembership() {
  }

  public JpaWorkspaceMembership(
      UUID id, UUID workspaceId, UUID userId, String role, Instant joinedAt) {
    this.id = id;
    this.workspaceId = workspaceId;
    this.userId = userId;
    this.role = role;
    this.joinedAt = joinedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getWorkspaceId() {
    return workspaceId;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getRole() {
    return role;
  }

  public Instant getJoinedAt() {
    return joinedAt;
  }
}
