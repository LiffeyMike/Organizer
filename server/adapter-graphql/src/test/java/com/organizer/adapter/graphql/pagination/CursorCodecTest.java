package com.organizer.adapter.graphql.pagination;

import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.organizer.adapter.graphql.error.BadUserInputException;
import com.organizer.adapter.graphql.pagination.CursorCodec.CursorPosition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CursorCodecTest {

  @Test
  void roundTripsEncodeAndDecode() {
    Instant sortKey = Instant.parse("2026-01-01T00:00:00Z");
    UUID id = UUID.randomUUID();

    String cursor = CursorCodec.encode(sortKey, id);
    CursorPosition decoded = CursorCodec.decode(cursor);

    assertThat(decoded.sortKey()).isEqualTo(sortKey);
    assertThat(decoded.id()).isEqualTo(id);
  }

  void decodingInvalidBase64Throws() {
    assertThatThrownBy(() -> CursorCodec.decode("Not valid base64!"))
        .isInstanceOf(BadUserInputException.class);

  }

  void decodingValidBase64WithTheWrongShapeThrows() {
    String garbage = Base64.getUrlEncoder().withoutPadding()
        .encodeToString("nonsense".getBytes());

    assertThatThrownBy(() -> CursorCodec.decode(garbage))
        .isInstanceOf(BadUserInputException.class);
  }
}
