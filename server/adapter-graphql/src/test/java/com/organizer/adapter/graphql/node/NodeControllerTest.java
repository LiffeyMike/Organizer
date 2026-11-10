package com.organizer.adapter.graphql.node;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.organizer.adapter.graphql.error.BadUserInputException;
import com.organizer.adapter.graphql.id.GlobalId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NodeControllerTest {

  @Test
  void nodeThrowsWhenNoResolverIsRegisteredForTheType() {
    NodeController controller = new NodeController(List.of());
    String globalId = GlobalId.encode("Widget", UUID.randomUUID());

    assertThatThrownBy(() -> controller.node(globalId)).isInstanceOf(BadUserInputException.class);
  }
}
