package com.organizer.adapter.graphql.pagination;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import com.organizer.adapter.graphql.error.BadUserInputException;

public final class CursorCodec {
  private CursorCodec() {
  }

  public record CursorPosition(Instant sortKey, UUID id) {
  }

  public static String encode(Instant sortKey, UUID id) {
    String raw = sortKey.toString() + ":" + id;
    return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  public static CursorPosition decode(String cursor) {
    try {
      String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
      int colon = raw.lastIndexOf(":");
      Instant sortKey = Instant.parse(raw.substring(0, colon));
      UUID id = UUID.fromString(raw.substring(colon + 1));
      return new CursorPosition(sortKey, id);
    } catch (Exception e) {
      throw new BadUserInputException("Malformed cursor: " + cursor);
    }
  }
}
