package com.organizer.adapter.persistence.workspace;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.organizer.application.port.out.WorkspaceRepository;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.coreconfig.page.Page;
import com.organizer.coreconfig.page.PageQuery;
import com.organizer.domain.workspace.Workspace;

@Component
public class WorkspaceRepositoryAdapter implements WorkspaceRepository {
  private final JpaWorkspaceRepository jpaRepository;
  private final WorkspaceMapper mapper;

  public WorkspaceRepositoryAdapter(JpaWorkspaceRepository jpaRepository, WorkspaceMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Workspace save(Workspace workspace) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(workspace)));
  }

  @Override
  public Optional<Workspace> findById(WorkspaceId id) {
    return jpaRepository.findById(id.id()).map(mapper::toDomain);
  }

  @Override
  public Page<Workspace> findPageForUser(UserId userId, PageQuery pageQuery) {
    boolean hasCursor = pageQuery.afterSortKey() != null;
    int fetchLimit = pageQuery.limit() + 1;

    List<JpaWorkspace> rows = pageQuery.backward()
        ? jpaRepository.findBackwardPageForUser(userId.id(), hasCursor, pageQuery.afterSortKey(),
            pageQuery.afterId(), fetchLimit)
        : jpaRepository.findForwardPageForUser(userId.id(), hasCursor, pageQuery.afterSortKey(),
            pageQuery.afterId(), fetchLimit);

    boolean hasMore = rows.size() > pageQuery.limit();
    List<Workspace> items = (hasMore ? rows.subList(0, pageQuery.limit()) : rows)
        .stream().map(mapper::toDomain).toList();

    return pageQuery.backward() ? new Page<>(items, hasCursor, hasMore) : new Page<>(items, hasMore, hasCursor);

  }
}
