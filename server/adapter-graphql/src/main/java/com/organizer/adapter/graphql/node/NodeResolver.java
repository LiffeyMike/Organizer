package com.organizer.adapter.graphql.node;

import com.organizer.coreconfig.id.UserId;
import java.util.Optional;
import java.util.UUID;

public interface NodeResolver {
  String typeName();

  Optional<Node> resolve(UserId callerId, UUID id);
}
