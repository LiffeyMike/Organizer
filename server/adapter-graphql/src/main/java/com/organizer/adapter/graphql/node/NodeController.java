package com.organizer.adapter.graphql.node;

import com.organizer.adapter.graphql.context.AuthContext;
import com.organizer.adapter.graphql.error.BadUserInputException;
import com.organizer.adapter.graphql.id.GlobalId;
import com.organizer.adapter.security.AuthenticatedUser;
import com.organizer.coreconfig.id.UserId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class NodeController {

  private final Map<String, NodeResolver> resolversByType;

  public NodeController(List<NodeResolver> resolvers) {
    this.resolversByType =
        resolvers.stream().collect(Collectors.toMap(NodeResolver::typeName, Function.identity()));
  }

  @QueryMapping
  public Node node(
      @Argument String id,
      @ContextValue(name = AuthContext.PRINCIPAL_KEY, required = false)
          AuthenticatedUser principal) {
    return resolveOne(AuthContext.requireUserId(principal), id);
  }

  @QueryMapping
  public List<Node> nodes(
      @Argument List<String> ids,
      @ContextValue(name = AuthContext.PRINCIPAL_KEY, required = false)
          AuthenticatedUser principal) {
    UserId callerId = AuthContext.requireUserId(principal);
    return ids.stream().map(id -> resolveOne(callerId, id)).toList();
  }

  private Node resolveOne(UserId callerId, String globalId) {
    String typeName = GlobalId.typeNameOf(globalId);
    NodeResolver resolver = resolversByType.get(typeName);
    if (resolver == null) {
      throw new BadUserInputException("Unknown type in id: " + globalId);
    }
    return resolver.resolve(callerId, GlobalId.decode(globalId, typeName)).orElse(null);
  }
}
