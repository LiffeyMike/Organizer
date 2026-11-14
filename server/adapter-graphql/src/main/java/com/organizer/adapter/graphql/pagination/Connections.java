package com.organizer.adapter.graphql.pagination;

import com.organizer.adapter.graphql.error.BadUserInputException;
import com.organizer.coreconfig.page.Page;
import com.organizer.coreconfig.page.PageQuery;
import java.util.List;
import java.util.function.Function;

public final class Connections {

  static final int DEFAULT_PAGE_SIZE = 20;
  static final int MAX_PAGE_SIZE = 100;

  private Connections() {}

  public static PageQuery pageQuery(Integer first, String after, Integer last, String before) {
    boolean backward = last != null || before != null;
    if (backward && (first != null || after != null)) {
      throw new BadUserInputException("Use first/after or last/before, not both");
    }

    int limit = limit(backward ? last : first);
    String cursor = backward ? before : after;

    if (cursor == null) {
      return new PageQuery(limit, null, null, backward);
    }

    CursorPosition position = CursorCodec.decode(cursor);
    return new PageQuery(limit, position.sortKey(), position.id(), backward);
  }

  public static <D, N> Connection<N> of(
      Page<D> page, Function<D, CursorPosition> position, Function<D, N> toNode) {
    List<Edge<N>> edges =
        page.items().stream()
            .map(item -> new Edge<>(encode(position.apply(item)), toNode.apply(item)))
            .toList();

    PageInfo pageInfo =
        new PageInfo(
            page.hasNextPage(),
            page.hasPreviousPage(),
            edges.isEmpty() ? null : edges.getFirst().cursor(),
            edges.isEmpty() ? null : edges.getLast().cursor());

    return new Connection<>(edges, pageInfo);
  }

  private static int limit(Integer requested) {
    if (requested == null) {
      return DEFAULT_PAGE_SIZE;
    }

    if (requested < 1 || requested > MAX_PAGE_SIZE) {
      throw new BadUserInputException("Page size must be between 1 and " + MAX_PAGE_SIZE);
    }

    return requested;
  }

  private static String encode(CursorPosition position) {
    return CursorCodec.encode(position.sortKey(), position.id());
  }
}
