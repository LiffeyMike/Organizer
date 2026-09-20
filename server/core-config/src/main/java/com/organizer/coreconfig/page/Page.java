package com.organizer.coreconfig.page;

import java.util.List;

public record Page<T>(List<T> items, boolean hasNextPage, boolean hasPreviousPage) {
  public static <T> Page<T> empty() {
    return new Page<>(List.of(), false, false);
  }
}
