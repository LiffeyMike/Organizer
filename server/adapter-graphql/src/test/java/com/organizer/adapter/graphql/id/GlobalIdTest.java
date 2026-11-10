package com.organizer.adapter.graphql.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.organizer.adapter.graphql.error.BadUserInputException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GlobalIdTest {

  @Test
  void roundTripsEncodeAndDecode() {
    UUID id = UUID.randomUUID();
    String globalId = GlobalId.encode("Widget", id);

    assertThat(globalId).isEqualTo("gid://organizer/Widget/" + id);
    assertThat(GlobalId.decode(globalId, "Widget")).isEqualTo(id);
  }

  @Test
  void decodingWithWrongExpectedTypeThrows() {
    String globalId = GlobalId.encode("Widget", UUID.randomUUID());

    assertThatThrownBy(() -> GlobalId.decode(globalId, "Gadget"))
        .isInstanceOf(BadUserInputException.class);
  }

  @Test
  void decodingAMalformedPrefixThrows() {
    assertThatThrownBy(() -> GlobalId.decode("not-a-global-id", "Widget"))
        .isInstanceOf(BadUserInputException.class);
  }

  @Test
  void decodingANonUuidIdSegmentThrows() {
    assertThatThrownBy(() -> GlobalId.decode("gid://organizer/Widget/not-a-uuid", "Wdiget"))
        .isInstanceOf(BadUserInputException.class);
  }
}
