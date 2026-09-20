package com.organizer.adapter.graphql.id;

import com.organizer.adapter.graphql.error.BadUserInputException;

import java.util.UUID;

public final class GlobalId {
  private static String PREFIX = "gid://organizer/";

  private GlobalId() {
  }

  public static String encode(String typename, UUID id) {
    return PREFIX + typename + "/" + id;
  }

  public static UUID decode(String globalId, String expectedTypeName) {
    if (globalId == null || !globalId.startsWith(PREFIX)) {
      throw new BadUserInputException("Malformed id: " + globalId);
    }

    String remainder = globalId.substring(PREFIX.length());
    int slash = remainder.indexOf("/");
    if (slash < 0) {
      throw new BadUserInputException("Malformed id: " + globalId);
    }
    String typeName = remainder.substring(0, slash);
    String rawId = remainder.substring(slash + 1);
    if (!typeName.equals(expectedTypeName)) {
      throw new BadUserInputException(
          "Expected a " + expectedTypeName + " id but got a " + typeName + " id");
    }

    try {
      return UUID.fromString(rawId);
    } catch (IllegalArgumentException e) {
      throw new BadUserInputException("Malformed id: " + globalId);
    }
  }
}
