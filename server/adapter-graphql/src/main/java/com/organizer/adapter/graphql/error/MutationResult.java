package com.organizer.adapter.graphql.error;

import java.util.List;

public record MutationResult<T>(T value, List<UserError> userErrors) {
  public static <T> MutationResult<T> ok(T value) {
    return new MutationResult<>(value, List.of());
  }

  public static <T> MutationResult<T> failure(List<UserError> errors) {
    return new MutationResult<T>(null, errors);
  }
}
