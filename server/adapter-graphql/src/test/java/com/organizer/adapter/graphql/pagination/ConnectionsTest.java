package com.organizer.adapter.graphql.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.organizer.adapter.graphql.error.BadUserInputException;
import com.organizer.coreconfig.page.Page;
import com.organizer.coreconfig.page.PageQuery;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ConnectionsTest {

  private static final CursorPosition POSITION =
      new CursorPosition(Instant.parse("2026-01-01T00:00:00Z"), UUID.randomUUID());
  private static final String CURSOR = CursorCodec.encode(POSITION.sortKey(), POSITION.id());

  @Nested
  class PageQueryFromRelayArgurments {

    @Test
    void noArgumentsIsTheFirstDefaultSizedPage() {
      assertThat(Connections.pageQuery(null, null, null, null))
          .isEqualTo(new PageQuery(Connections.DEFAULT_PAGE_SIZE, null, null, false));
    }

    @Test
    void firstAndAfterPageForwardFromCursor() {
      assertThat(Connections.pageQuery(5, CURSOR, null, null))
          .isEqualTo(new PageQuery(5, POSITION.sortKey(), POSITION.id(), false));
    }

    @Test
    void lastWithoutBeforeIsNewestPage() {
      assertThat(Connections.pageQuery(null, null, 5, null))
          .isEqualTo(new PageQuery(5, null, null, true));
    }

    @Test
    void lastAndBeforePageBackwardFromCursor() {
      assertThat(Connections.pageQuery(null, null, 5, CURSOR))
          .isEqualTo(new PageQuery(5, POSITION.sortKey(), POSITION.id(), true));
    }

    @ParameterizedTest
    @MethodSource("com.organizer.adapter.graphql.pagination.ConnectionsTest#mixedDirections")
    void mixingDirectionsIsRejected(Integer first, String after, Integer last, String before) {
      assertThatThrownBy(() -> Connections.pageQuery(first, after, last, before))
          .isInstanceOf(BadUserInputException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Connections.MAX_PAGE_SIZE + 1})
    void pageSizeOutsideOneToMaxIsRejected(int size) {
      assertThatThrownBy(() -> Connections.pageQuery(size, null, null, null))
          .isInstanceOf(BadUserInputException.class);

      assertThatThrownBy(() -> Connections.pageQuery(null, null, size, null))
          .isInstanceOf(BadUserInputException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, Connections.MAX_PAGE_SIZE})
    void pageSizesAtTheBoundsAreAccepted(int size) {
      assertThat(Connections.pageQuery(size, null, null, null).limit()).isEqualTo(size);
      assertThat(Connections.pageQuery(null, null, size, null).limit()).isEqualTo(size);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not a cursor"})
    void malformedCursorsAreRejected(String cursor) {
      assertThatThrownBy(() -> Connections.pageQuery(null, cursor, null, null))
          .isInstanceOf(BadUserInputException.class);

      assertThatThrownBy(() -> Connections.pageQuery(null, null, null, cursor))
          .isInstanceOf(BadUserInputException.class);
    }
  }

  @Nested
  class ConnectionsFromPage {

    private final Item a = item("a", "2026-01-01T00:00:00Z");
    private final Item b = item("b", "2026-01-02T00:00:00Z");
    private final Item c = item("c", "2026-01-03T00:00:00Z");

    @Test
    void connectionCursorsRoundTripToTheItemsPositions() {
      Connection<String> connection =
          Connections.of(new Page<>(List.of(a, b, c), false, false), Item::position, Item::name);

      assertThat(connection.edges()).extracting(Edge::node).containsExactly("a", "b", "c");
      assertThat(connection.edges())
          .extracting(edge -> CursorCodec.decode(edge.cursor()))
          .containsExactly(a.position(), b.position(), c.position());
    }

    @Test
    void pageInfoCarriesTheFlagsAndTheOuterCursors() {
      Connection<String> connection =
          Connections.of(new Page<>(List.of(a, b, c), true, false), Item::position, Item::name);

      PageInfo pageInfo = connection.pageInfo();
      assertThat(pageInfo.hasNextPage()).isTrue();
      assertThat(pageInfo.hasPreviousPage()).isFalse();
      assertThat(pageInfo.startCursor()).isEqualTo(connection.edges().getFirst().cursor());
      assertThat(pageInfo.endCursor()).isEqualTo(connection.edges().getLast().cursor());
      assertThat(pageInfo.startCursor()).isNotEqualTo(pageInfo.endCursor());
    }

    @Test
    void emptyPageHasNullCursor() {
      Connection<String> connection =
          Connections.of(Page.<Item>empty(), Item::position, Item::name);

      assertThat(connection.edges()).isEmpty();
      assertThat(connection.pageInfo()).isEqualTo(new PageInfo(false, false, null, null));
    }
  }

  private record Item(CursorPosition position, String name) {}

  private static Item item(String name, String createdAt) {
    return new Item(new CursorPosition(Instant.parse(createdAt), UUID.randomUUID()), name);
  }

  static Stream<Arguments> mixedDirections() {
    return Stream.of(
        Arguments.of(5, null, 5, null),
        Arguments.of(5, null, null, CURSOR),
        Arguments.of(5, null, null, CURSOR),
        Arguments.of(null, CURSOR, 5, null));
  }
}
