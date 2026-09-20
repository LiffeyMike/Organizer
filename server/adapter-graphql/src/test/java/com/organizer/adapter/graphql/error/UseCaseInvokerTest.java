package com.organizer.adapter.graphql.error;

import org.junit.jupiter.api.Test;

import com.organizer.coreconfig.error.ConflictException;
import com.organizer.coreconfig.error.ForbiddenException;
import com.organizer.coreconfig.error.InvalidCredentialsException;
import com.organizer.coreconfig.error.NotFoundException;
import com.organizer.coreconfig.error.UnauthenticatedException;
import com.organizer.coreconfig.error.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UseCaseInvokerTest {

  private final UseCaseInvoker invoker = new UseCaseInvoker();

  @Test
  void returnsValueWithNoErrorsOnSuccess() {
    MutationResult<String> result = invoker.invoke(() -> "ok");

    assertThat(result.value()).isEqualTo("ok");
    assertThat(result.userErrors()).isEmpty();
  }

  @Test
  void mapsForbiddenExceptionToForbiddenUserError() {
    MutationResult<String> result = invoker.invoke(() -> {
      throw new ForbiddenException("nope");
    });

    assertThat(result.value()).isNull();
    assertThat(result.userErrors()).hasSize(1);
    assertThat(result.userErrors().get(0).code()).isEqualTo(UserErrorCode.FORBIDDEN);
  }

  @Test
  void mapsNotFoundExceptionToUserError() {
    MutationResult<String> result = invoker.invoke(() -> {
      throw new NotFoundException("nope");
    });

    assertThat(result.userErrors().get(0).code()).isEqualTo(UserErrorCode.NOT_FOUND);
  }

  @Test
  void mapsValidationExceptionToUserError() {
    MutationResult<String> result = invoker.invoke(() -> {
      throw new ValidationException("nope");
    });

    assertThat(result.userErrors().get(0).code()).isEqualTo(UserErrorCode.VALIDATION);
  }

  @Test
  void mapsConflictExceptionToUserError() {
    MutationResult<String> result = invoker.invoke(() -> {
      throw new ConflictException("nope");
    });

    assertThat(result.userErrors().get(0).code()).isEqualTo(UserErrorCode.CONFLICT);
  }

  @Test
  void mapsInvalidCredentialsExceptionToUserError() {
    MutationResult<String> result = invoker.invoke(() -> {
      throw new InvalidCredentialsException("nope");
    });

    assertThat(result.userErrors().get(0).code()).isEqualTo(UserErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  void unauthenticatedExceptionPropogatesUncaught() {
    assertThatThrownBy(() -> invoker.invoke(() -> {
      throw new UnauthenticatedException("no token");
    })).isInstanceOf(UnauthenticatedException.class);
  }
}
