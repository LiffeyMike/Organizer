package com.organizer.adapter.graphql.node;

import java.util.Optional;
import java.util.UUID;

public interface NodeResolver {
  String typeName();

  Optional<Node> resolve(UUID id);
}
