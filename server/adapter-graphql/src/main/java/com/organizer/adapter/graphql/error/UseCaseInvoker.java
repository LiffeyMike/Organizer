package com.organizer.adapter.graphql.error;

import java.util.List;
import java.util.function.Supplier;

import com.organizer.coreconfig.error.ConflictException;
import com.organizer.coreconfig.error.DomainException;
import com.organizer.coreconfig.error.ForbiddenException;
import com.organizer.coreconfig.error.InvalidCredentialsException;
import com.organizer.coreconfig.error.NotFoundException;
import com.organizer.coreconfig.error.ValidationException;

public class UseCaseInvoker {

  public <T> MutationResult<T> invoke(Supplier<T> useCaseCall) {
    try {
      return MutationResult.ok(useCaseCall.get());
    } catch (ForbiddenException | NotFoundException | ValidationException | ConflictException
        | InvalidCredentialsException e) {
      return MutationResult.failure(List.of(UserError.from((DomainException) e)));
    }
  }
}
