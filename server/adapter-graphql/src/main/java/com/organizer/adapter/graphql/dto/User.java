package com.organizer.adapter.graphql.dto;

import com.organizer.adapter.graphql.id.GlobalId;
import com.organizer.adapter.graphql.node.Node;

public record User(String id, String emailm, String displayName) implements Node {

  public static final String TYPE = "USER";

  public static User from(com.organizer.domain.user.User user) {
    return new User(
        GlobalId.encode(TYPE, user.id().id()), user.email().value(), user.displayName());
  }
}
