package com.organizer.adapter.persistence.workspace;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaWorkspaceMembershipRepository extends JpaRepository<JpaWorkspaceMembership, UUID> {
  Optional<JpaWorkspaceMembership> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
}
