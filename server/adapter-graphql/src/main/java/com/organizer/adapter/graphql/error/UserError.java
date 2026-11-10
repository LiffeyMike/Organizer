package com.organizer.adapter.graphql.error;

import com.organizer.coreconfig.error.ConflictException;
import com.organizer.coreconfig.error.DomainException;
import com.organizer.coreconfig.error.ForbiddenException;
import com.organizer.coreconfig.error.InvalidCredentialsException;
import com.organizer.coreconfig.error.NotFoundException;
import com.organizer.coreconfig.error.ValidationException;
import java.util.List;

public record UserError(List<String> field, String message, UserErrorCode code) {

  public static UserError from(DomainException e) {
    UserErrorCode code =
        switch (e) {
          case NotFoundException ignored -> UserErrorCode.NOT_FOUND;
          case ForbiddenException ignored -> UserErrorCode.FORBIDDEN;
          case ConflictException ignored -> UserErrorCode.CONFLICT;
          case InvalidCredentialsException ignored -> UserErrorCode.INVALID_CREDENTIALS;
          case ValidationException ignored -> UserErrorCode.VALIDATION;
          default ->
              throw new IllegalArgumentException(
                  "Unmapped DomainException type for userErrors: " + e.getClass());
        };
    return new UserError(null, e.getMessage(), code);
  }
}
