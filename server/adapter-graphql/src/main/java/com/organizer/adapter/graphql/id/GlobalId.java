package com.organizer.adapter.graphql.id;

import com.organizer.adapter.graphql.error.BadUserInputException;
import java.util.UUID;

public final class GlobalId {
  private static final String PREFIX = "gid://organizer/";

  private GlobalId() {}

  public static String encode(String typename, UUID id) {
    return PREFIX + typename + "/" + id;
  }

  public static String typeNameOf(String globalId) {
    if (globalId == null || !globalId.startsWith(PREFIX)) {
      throw malformed(globalId);
    }

    int slash = globalId.indexOf('/', PREFIX.length());
    if (slash <= PREFIX.length()) {
      throw malformed(globalId);
    }

    return globalId.substring(PREFIX.length(), slash);
  }

  public static UUID decode(String globalId, String expectedTypeName) {
    String typeName = typeNameOf(globalId);

    if (!typeName.equals(expectedTypeName)) {
      throw new BadUserInputException(
          "Expected a " + expectedTypeName + " id but got a " + typeName + " id");
    }

    try {
      return UUID.fromString(globalId.substring(PREFIX.length() + typeName.length() + 1));
    } catch (IllegalArgumentException e) {
      throw malformed(globalId);
    }
  }

  private static BadUserInputException malformed(String globalId) {
    return new BadUserInputException("Malformed id: " + globalId);
  }
}
