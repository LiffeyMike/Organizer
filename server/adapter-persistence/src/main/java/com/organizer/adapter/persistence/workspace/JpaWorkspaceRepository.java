package com.organizer.adapter.persistence.workspace;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface JpaWorkspaceRepository extends JpaRepository<JpaWorkspace, UUID> {

  @Query(value = """
      SELECT w.* FROM workspaces w
      JOIN workspace_memberships m ON m.workspace_id = w.id
      WHERE m.user_id = :userId
        AND (:hasCursor = false OR (w.created_at, w.id) > (:afterSortKey, :afterId))
      ORDER BY w.created_at ASC, w.id ASC
      LIMIT :fetchLimit
        """, nativeQuery = true)
  List<JpaWorkspace> findForwardPageForUser(
      @Param("userId") UUID userId, @Param("hasCursor") boolean hasCursor,
      @Param("afterSortKey") Instant afterSortKey, @Param("afterId") UUID afterId,
      @Param("fetchLimit") int fetchLimit);

  @Query(value = """
      SELECT w.* FROM workspaces w
      JOIN workspace_memberships m ON m.workspace_id = w.id
      WHERE m.user_id = :userId
        AND (:hasCursor = false OR (w.created_at, w.id) < (:afterSortKey, :afterId))
      ORDER BY w.created_at DESC, w.id DESC
      LIMIT :fetchLimit
      """, nativeQuery = true)
  List<JpaWorkspace> findBackwardPageForUser(
      @Param("userId") UUID userId, @Param("hasCursor") boolean hasCursor,
      @Param("afterSortKey") Instant afterSortKey, @Param("afterId") UUID afterId,
      @Param("fetchLimit") int fetchLimit);
}
