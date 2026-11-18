package com.organizer.adapter.graphql.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.organizer.adapter.graphql.context.AuthContext;
import com.organizer.adapter.graphql.error.UseCaseInvoker;
import com.organizer.adapter.graphql.pagination.CursorCodec;
import com.organizer.adapter.graphql.pagination.CursorPosition;
import com.organizer.adapter.graphql.pagination.PageInfo;
import com.organizer.adapter.security.AuthenticatedUser;
import com.organizer.application.workspace.ListMyWorkspacesUseCase;
import com.organizer.coreconfig.id.UserId;
import com.organizer.coreconfig.id.WorkspaceId;
import com.organizer.coreconfig.page.Page;
import com.organizer.coreconfig.page.PageQuery;
import com.organizer.domain.user.Email;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@GraphQlTest(WorkspaceController.class)
@Import(UseCaseInvoker.class)
class WorkspaceControllerTest {

  private static final UserId EAMONN = new UserId(UUID.randomUUID());
  private static final AuthenticatedUser AUTHENTICATED_USER =
      new AuthenticatedUser(EAMONN, new Email("eamonn@dublin.city"));
  private static final com.organizer.domain.workspace.Workspace MY_ORGANIZER =
      com.organizer.domain.workspace.Workspace.personalFor(
          new WorkspaceId(UUID.randomUUID()),
          EAMONN,
          "Eamonn",
          Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

  @Autowired ExecutionGraphQlServiceTester graphQlTester;

  @MockitoBean ListMyWorkspacesUseCase listMyWorkspacesUseCase;

  private GraphQlTester as(AuthenticatedUser principal) {
    return graphQlTester
        .mutate()
        .configureExecutionInput((input, builder) ->
            builder.graphQLContext(ctx -> ctx.put(AuthContext.PRINCIPAL_KEY, principal)).build())
        .build();
  }

  @Test
  void myWorkspacesWithoutAPrincipalIsUnauthenticated() {
    graphQlTester
        .document(
            """
              query GetMyWorkspaces {
                myWorkspaces {
                  edges { node { id} }
                  pageInfo { hasNextPage hasPreviousPage startCursor endCursor }
                }
              }
            """)
        .execute()
        .errors()
        .satisfy(errors ->
            assertThat(errors)
                .singleElement()
                .satisfies(error ->
                    assertThat(error.getExtensions()).containsEntry("code", "UNAUTHENTICATED")));
    verifyNoInteractions(listMyWorkspacesUseCase);
  }

  @Test
  void noArgumentsAskForTheDefaultFirstPage() {
    PageQuery defaultFirstPage = new PageQuery(20, null, null, false);
    Page<com.organizer.domain.workspace.Workspace> defaultPage =
        new Page<com.organizer.domain.workspace.Workspace>(List.of(MY_ORGANIZER), false, false);

    when(listMyWorkspacesUseCase.execute(EAMONN, defaultFirstPage)).thenReturn(defaultPage);

    as(AUTHENTICATED_USER)
        .document(
            """
            {
              myWorkspaces {
                  edges { node { id } }
                  pageInfo { hasNextPage hasPreviousPage startCursor endCursor }
              }
            }
            """)
        .execute()
        .path("myWorkspaces.edges[0].node.id")
        .entity(String.class)
        .isEqualTo("gid://organizer/Workspace/" + MY_ORGANIZER.id().id());
  }

  @Test
  void edgesCarryGlobalIdsAndCursorsAndPageInfoIsPassedThrough() {
    PageQuery defaultFirstPage = new PageQuery(20, null, null, false);
    String pageCursor = CursorCodec.encode(MY_ORGANIZER.createdAt(), MY_ORGANIZER.id().id());
    PageInfo expectedPageInfo = new PageInfo(true, false, pageCursor, pageCursor);
    Page<com.organizer.domain.workspace.Workspace> defaultPage =
        new Page<com.organizer.domain.workspace.Workspace>(List.of(MY_ORGANIZER), true, false);

    when(listMyWorkspacesUseCase.execute(EAMONN, defaultFirstPage)).thenReturn(defaultPage);

    as(AUTHENTICATED_USER)
        .document(
            """
            {
              myWorkspaces {
                  edges { cursor node { id } }
                  pageInfo { hasNextPage hasPreviousPage startCursor endCursor }
              }
            }
            """)
        .execute()
        .path("myWorkspaces.edges[0].node.id")
        .entity(String.class)
        .isEqualTo("gid://organizer/Workspace/" + MY_ORGANIZER.id().id())
        .path("myWorkspaces.edges[0].cursor")
        .entity(String.class)
        .satisfies(cursor ->
            assertThat(CursorCodec.decode(cursor))
                .isEqualTo(new CursorPosition(MY_ORGANIZER.createdAt(), MY_ORGANIZER.id().id())))
        .path("myWorkspaces.pageInfo")
        .entity(PageInfo.class)
        .isEqualTo(expectedPageInfo);
  }

  @Test
  void anEdgeCursorFetchesTheNextPage() {
    com.organizer.domain.workspace.Workspace sideProject =
        com.organizer.domain.workspace.Workspace.personalFor(
            new WorkspaceId(UUID.randomUUID()),
            EAMONN,
            "Eamonn",
            Clock.fixed(Instant.EPOCH.plusSeconds(60), ZoneOffset.UTC));

    when(listMyWorkspacesUseCase.execute(EAMONN, new PageQuery(20, null, null, false)))
        .thenReturn(new Page<>(List.of(MY_ORGANIZER), true, false));
    when(listMyWorkspacesUseCase.execute(
            EAMONN, new PageQuery(20, MY_ORGANIZER.createdAt(), MY_ORGANIZER.id().id(), false)))
        .thenReturn(new Page<>(List.of(sideProject), false, true));

    String cursor =
        as(AUTHENTICATED_USER)
            .document("{ myWorkspaces { edges { cursor } } }")
            .execute()
            .path("myWorkspaces.edges[0].cursor")
            .entity(String.class)
            .get();

    as(AUTHENTICATED_USER)
        .document(
            """
              query NextPage($after: String) {
                myWorkspaces(after: $after) {
                  edges { node { id } }
                }
              }
            """)
        .variable("after", cursor)
        .execute()
        .path("myWorkspaces.edges[0].node.id")
        .entity(String.class)
        .isEqualTo("gid://organizer/Workspace/" + sideProject.id().id());
  }

  @Test
  void invalidPagingArgumentsAreBadInput() {
    as(AUTHENTICATED_USER)
        .document(
            """
              query InvalidRequest($first: Int, $last: Int) {
                myWorkspaces(first: $first, last: $last) {
                  edges { node { id } }
                }
              }
            """)
        .variables(
            Map.of(
                "first", 1,
                "last", 1))
        .execute()
        .errors()
        .satisfy(errors ->
            assertThat(errors)
                .singleElement()
                .satisfies(error ->
                    assertThat(error.getExtensions()).containsEntry("code", "BAD_USER_INPUT")));

    verifyNoInteractions(listMyWorkspacesUseCase);
  }
}
