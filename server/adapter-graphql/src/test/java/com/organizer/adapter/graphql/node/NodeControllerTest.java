package com.organizer.adapter.graphql.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.organizer.adapter.graphql.error.BadUserInputException;
import com.organizer.adapter.graphql.id.GlobalId;
import com.organizer.adapter.security.AuthenticatedUser;
import com.organizer.coreconfig.error.UnauthenticatedException;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NodeControllerTest {

  private static final AuthenticatedUser SIDESHOW_BOB =
      new AuthenticatedUser(new UserId(UUID.randomUUID()), new Email("s.bob@krusty.clown"));

  private static final UUID VISIBLE = UUID.randomUUID();
  private static final UUID HIDDEN = UUID.randomUUID();

  public record Widget(String id) implements Node {}

  private static final class StubResolver implements NodeResolver {
    private final Set<UUID> visible;
    final List<UserId> callers = new ArrayList<>();
    final List<UUID> ids = new ArrayList<>();

    StubResolver(UUID... visible) {
      this.visible = Set.of(visible);
    }

    @Override
    public String typeName() {
      return "Widget";
    }

    @Override
    public Optional<Node> resolve(UserId callerId, UUID id) {
      callers.add(callerId);
      ids.add(id);
      return visible.contains(id) ? Optional.of(widget(id)) : Optional.empty();
    }
  }

  private static Widget widget(UUID id) {
    return new Widget(GlobalId.encode("Widget", id));
  }

  private static String gid(UUID id) {
    return GlobalId.encode("Widget", id);
  }

  @Nested
  class Startup {

    @Test
    void twoResolversForTheSameTypeFailAtConstruction() {
      assertThatThrownBy(() -> new NodeController(List.of(new StubResolver(), new StubResolver())))
          .isInstanceOf(IllegalStateException.class);
    }
  }

  @Nested
  class Authentication {

    @Test
    void nodeWithoutAPrincipalIsUnauthenticated() {
      StubResolver resolver = new StubResolver(VISIBLE);
      NodeController controller = new NodeController(List.of(resolver));

      assertThatThrownBy(() -> controller.node(gid(VISIBLE), null))
          .isInstanceOf(UnauthenticatedException.class);
      assertThat(resolver.ids).isEmpty();
    }

    @Test
    void nodesWithoutAPrincipalIsUnauthenticated() {
      StubResolver resolver = new StubResolver(VISIBLE);
      NodeController controller = new NodeController(List.of(resolver));

      assertThatThrownBy(() -> controller.nodes(List.of(gid(VISIBLE)), null))
          .isInstanceOf(UnauthenticatedException.class);
      assertThat(resolver.ids).isEmpty();
    }
  }

  @Nested
  class RequestShape {

    @Test
    void malformedIsBadUserInput() {
      NodeController controller = new NodeController(List.of(new StubResolver()));

      assertThatThrownBy(() -> controller.node("not-there", SIDESHOW_BOB))
          .isInstanceOf(BadUserInputException.class);
    }

    @Test
    void idForAnUnregisteredTypeIsBadUserInput() {
      NodeController controller = new NodeController(List.of());

      assertThatThrownBy(() -> controller.node(gid(VISIBLE), SIDESHOW_BOB))
          .isInstanceOf(BadUserInputException.class);
    }

    @Test
    void idWithANonUuidSegmentIsBadUserInputAndNeverReachesResolver() {
      StubResolver resolver = new StubResolver();
      NodeController controller = new NodeController(List.of(resolver));

      assertThatThrownBy(() -> controller.node("gid://organizer/Widget/not-a-uuid", SIDESHOW_BOB))
          .isInstanceOf(BadUserInputException.class);

      assertThat(resolver.ids).isEmpty();
    }
  }

  @Nested
  class DispatchAndVisibility {

    @Test
    void nodePassesTheCallerAndTheDecodedIdToTheResolver() {
      StubResolver resolver = new StubResolver(VISIBLE);
      NodeController controller = new NodeController(List.of(resolver));

      Node node = controller.node(gid(VISIBLE), SIDESHOW_BOB);

      assertThat(node).isEqualTo(widget(VISIBLE));
      assertThat(resolver.callers).containsExactly(SIDESHOW_BOB.userId());
      assertThat(resolver.ids).containsExactly(VISIBLE);
    }

    @Test
    void hiddenNodeIsNullAndNotAnError() {
      NodeController controller = new NodeController(List.of(new StubResolver(VISIBLE)));

      assertThat(controller.node(gid(HIDDEN), SIDESHOW_BOB)).isNull();
    }

    @Test
    void nodesReturnsNullOnlyForHiddenElementsKeepingOrder() {
      StubResolver resolver = new StubResolver(VISIBLE);
      NodeController controller = new NodeController(List.of(resolver));

      List<Node> nodes = controller.nodes(List.of(gid(HIDDEN), gid(VISIBLE)), SIDESHOW_BOB);

      assertThat(nodes).containsExactly(null, widget(VISIBLE));
      assertThat(resolver.callers).containsOnly(SIDESHOW_BOB.userId());
    }
  }
}
