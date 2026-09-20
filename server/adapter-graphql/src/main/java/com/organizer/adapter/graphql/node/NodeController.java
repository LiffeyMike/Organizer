package com.organizer.adapter.graphql.node;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.organizer.adapter.graphql.error.BadUserInputException;
import com.organizer.adapter.graphql.id.GlobalId;

@Controller
public class NodeController {

  private final Map<String, NodeResolver> resolversByType;

  public NodeController(List<NodeResolver> resolvers) {
    this.resolversByType = resolvers.stream()
        .collect(Collectors.toMap(NodeResolver::typeName, r -> r));
  }

  @QueryMapping
  public Object node(@Argument String id) {
    return resolveOne(id);
  }

  @QueryMapping
  public List<Object> nodes(@Argument List<String> ids) {
    return ids.stream().map(this::resolveOne).toList();
  }

  private Object resolveOne(String globalId) {
    String typeName = extractTypeName(globalId);
    NodeResolver resolver = resolversByType.get(typeName);
    if (resolver == null) {
      throw new BadUserInputException("Unknown type in id: " + globalId);
    }
    UUID id = GlobalId.decode(globalId, typeName);
    return resolver.resolve(id).orElse(null);
  }

  private String extractTypeName(String globalId) {
    String withoutPrefix = globalId.substring("gid://organizer/".length());
    return withoutPrefix.substring(0, withoutPrefix.indexOf("/"));
  }

}
