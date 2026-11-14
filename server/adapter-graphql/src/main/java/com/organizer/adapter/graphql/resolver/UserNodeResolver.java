package com.organizer.adapter.graphql.resolver;

import com.organizer.adapter.graphql.dto.User;
import com.organizer.adapter.graphql.node.Node;
import com.organizer.adapter.graphql.node.NodeResolver;
import com.organizer.application.user.GetUserUseCase;
import com.organizer.coreconfig.id.UserId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserNodeResolver implements NodeResolver {

  private final GetUserUseCase getUserUseCase;

  public UserNodeResolver(GetUserUseCase getUserUseCase) {
    this.getUserUseCase = getUserUseCase;
  }

  @Override
  public String typeName() {
    return User.TYPE;
  }

  @Override
  public Optional<Node> resolve(UserId userId, UUID id) {
    return getUserUseCase.execute(userId, new UserId(id)).map(User::from);
  }
}
