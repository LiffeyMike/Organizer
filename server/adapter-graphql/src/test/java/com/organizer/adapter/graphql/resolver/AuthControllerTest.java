package com.organizer.adapter.graphql.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.organizer.adapter.graphql.context.AuthContext;
import com.organizer.adapter.graphql.dto.Session;
import com.organizer.adapter.graphql.dto.User;
import com.organizer.adapter.graphql.error.UseCaseInvoker;
import com.organizer.adapter.graphql.error.UserError;
import com.organizer.adapter.security.AuthenticatedUser;
import com.organizer.application.user.AuthResult;
import com.organizer.application.user.GetCurrentUserUseCase;
import com.organizer.application.user.LoginCommand;
import com.organizer.application.user.LoginUseCase;
import com.organizer.application.user.RefreshTokenUseCase;
import com.organizer.application.user.RegisterCommand;
import com.organizer.application.user.RegisterUserUseCase;
import com.organizer.coreconfig.error.ConflictException;
import com.organizer.coreconfig.error.InvalidCredentialsException;
import com.organizer.coreconfig.error.NotFoundException;
import com.organizer.coreconfig.error.ValidationException;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.auth.TokenPair;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.HashedPassword;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@GraphQlTest(AuthController.class)
@Import(UseCaseInvoker.class)
class AuthControllerTest {

  private static final com.organizer.domain.user.User MORPHEUS =
      new com.organizer.domain.user.User(
          new UserId(UUID.randomUUID()),
          new Email("morpheus@matrix.net"),
          new HashedPassword("$2a$12$redpill"),
          "Morpheus",
          Instant.parse("1999-06-01T01:00:00Z"));

  private static final AuthResult SIGNED_IN =
      new AuthResult(MORPHEUS, new TokenPair("access-token", "refresh-token"));

  @Autowired ExecutionGraphQlServiceTester graphQlTester;

  @MockitoBean RegisterUserUseCase registerUserUseCase;
  @MockitoBean LoginUseCase loginUseCase;
  @MockitoBean RefreshTokenUseCase refreshTokenUseCase;
  @MockitoBean GetCurrentUserUseCase getCurrentUserUseCase;

  private GraphQlTester as(AuthenticatedUser principal) {
    return graphQlTester
        .mutate()
        .configureExecutionInput((input, builder) ->
            builder.graphQLContext(ctx -> ctx.put(AuthContext.PRINCIPAL_KEY, principal)).build())
        .build();
  }

  @Test
  void sessionCreateWithBadCredentialsIsAUserErrorNotATopLevelError() {
    when(loginUseCase.execute(new LoginCommand("morpheus@matrix.net", "wrong")))
        .thenThrow(new InvalidCredentialsException("Invalid email or password"));

    graphQlTester
        .document(
            """
            mutation {
              sessionCreate(input: {email: "morpheus@matrix.net", password: "wrong"}) {
                session { accessToken }
                userErrors { code message }
              }
            }
            """)
        .execute()
        .path("sessionCreate.session")
        .valueIsNull()
        .path("sessionCreate.userErrors[0].code")
        .entity(String.class)
        .isEqualTo("INVALID_CREDENTIALS");
  }

  @Test
  void meWithoutCredentialsIsATopLevelUnauthenticatedError() {
    graphQlTester
        .document("{ me { id } }")
        .execute()
        .errors()
        .satisfy(errors ->
            assertThat(errors)
                .singleElement()
                .satisfies(error ->
                    assertThat(error.getExtensions()).containsEntry("code", "UNAUTHENTICATED")));
    verifyNoInteractions(getCurrentUserUseCase);
  }

  @Test
  void meReturnsTheCallerWithAGlobalId() {
    when(getCurrentUserUseCase.execute(MORPHEUS.id())).thenReturn(MORPHEUS);

    as(new AuthenticatedUser(MORPHEUS.id(), MORPHEUS.email()))
        .document("{ me { id email } }")
        .execute()
        .path("me.id")
        .entity(String.class)
        .isEqualTo("gid://organizer/User/" + MORPHEUS.id().id())
        .path("me.email")
        .entity(String.class)
        .isEqualTo(MORPHEUS.email().value());
  }

  @Test
  void userRegisterReturnsTheUserAndTheSession() {
    when(registerUserUseCase.execute(
            new RegisterCommand(MORPHEUS.email().value(), "redpill", MORPHEUS.displayName())))
        .thenReturn(SIGNED_IN);

    User createdUser = User.from(MORPHEUS);
    Session createdSession = Session.from(SIGNED_IN);

    graphQlTester
        .document(
            """
              mutation Register($input: UserRegisterInput!) {
                userRegister(input: $input) {
                  user { id email displayName }
                  session { accessToken refreshToken user { id email displayName } }
                  userErrors { code message }
                }
              }
            """)
        .variable(
            "input",
            Map.of(
                "email", "morpheus@matrix.net",
                "password", "redpill",
                "displayName", "Morpheus"))
        .execute()
        .path("userRegister.user")
        .entity(User.class)
        .isEqualTo(createdUser)
        .path("userRegister.session")
        .entity(Session.class)
        .isEqualTo(createdSession)
        .path("userRegister.userErrors")
        .entityList(UserError.class)
        .hasSize(0);
  }

  @Test
  void userRegisterWithATakenEmailIsAConflictUserError() {
    when(registerUserUseCase.execute(
            new RegisterCommand(MORPHEUS.email().value(), "redpill", MORPHEUS.displayName())))
        .thenThrow(new ConflictException("An account with this email already exists"));

    graphQlTester
        .document(
            """
              mutation Register($input: UserRegisterInput!) {
                userRegister(input: $input) {
                  user { id email displayName }
                  session { accessToken refreshToken }
                  userErrors { code message }
                }
              }
            """)
        .variable(
            "input",
            Map.of(
                "email", "morpheus@matrix.net",
                "password", "redpill",
                "displayName", "Morpheus"))
        .execute()
        .path("userRegister.userErrors[0].code")
        .entity(String.class)
        .isEqualTo("CONFLICT")
        .path("userRegister.user")
        .valueIsNull()
        .path("userRegister.session")
        .valueIsNull();
  }

  @Test
  void userRegisterRejectedByValidationIsAValidationUserError() {
    when(registerUserUseCase.execute(new RegisterCommand("not-an-email", "redpill", "Morpheus")))
        .thenThrow(new ValidationException("Malformed email address"));

    graphQlTester
        .document(
            """
              mutation Register($input: UserRegisterInput!) {
                userRegister(input: $input) {
                  user { id email displayName }
                  userErrors { code message }
                }
              }
            """)
        .variable(
            "input",
            Map.of(
                "email", "not-an-email",
                "password", "redpill",
                "displayName", "Morpheus"))
        .execute()
        .path("userRegister.userErrors[0].code")
        .entity(String.class)
        .isEqualTo("VALIDATION");
  }

  @Test
  void sessionCreateReturnsASession() {
    Session session = Session.from(SIGNED_IN);
    when(loginUseCase.execute(new LoginCommand(MORPHEUS.email().value(), "redpill")))
        .thenReturn(SIGNED_IN);

    graphQlTester
        .document(
            """
              mutation Login($input: SessionCreateInput!) {
                sessionCreate(input: $input) {
                  session { accessToken refreshToken user { id email displayName } }
                }
              }
            """)
        .variable(
            "input",
            Map.of(
                "email", "morpheus@matrix.net",
                "password", "redpill"))
        .execute()
        .path("sessionCreate.session")
        .entity(Session.class)
        .isEqualTo(session);
  }

  @Test
  void sessionRefreshWithAReplayedTokenIsAUserError() {
    when(refreshTokenUseCase.execute("refreshToken"))
        .thenThrow(new InvalidCredentialsException("Invalid refresh token"));

    graphQlTester
        .document(
            """
              mutation Refresh($input: SessionRefreshInput!) {
                sessionRefresh(input: $input) {
                  userErrors { code message }
                }
              }
            """)
        .variable("input", Map.of("refreshToken", "refreshToken"))
        .execute()
        .path("sessionRefresh.userErrors[0].code")
        .entity(String.class)
        .isEqualTo("INVALID_CREDENTIALS");
  }

  @Test
  void meForAUserDeletedSinceTheTokenWasIssuedIsUnauthenticated() {
    when(getCurrentUserUseCase.execute(MORPHEUS.id()))
        .thenThrow(new NotFoundException("User not found"));

    as(new AuthenticatedUser(MORPHEUS.id(), MORPHEUS.email()))
        .document("{ me { id email displayName } }")
        .execute()
        .errors()
        .satisfy(errors ->
            assertThat(errors)
                .singleElement()
                .satisfies(error ->
                    assertThat(error.getExtensions()).containsEntry("code", "UNAUTHENTICATED")));
  }
}
