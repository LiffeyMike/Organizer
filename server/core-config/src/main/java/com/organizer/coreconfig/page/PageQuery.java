package com.organizer.coreconfig.page;

import java.time.Instant;
import java.util.UUID;

public record PageQuery(int limit, Instant afterSortKey, UUID afterId, boolean backward) {
  public static PageQuery firstPage(int limit) {
    return new PageQuery(limit, null, null, false);
  }
}
