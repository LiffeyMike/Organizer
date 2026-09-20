package com.organizer.adapter.graphql.error;

import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import com.organizer.coreconfig.error.UnauthenticatedException;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;

@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

  @Override
  protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
    if (ex instanceof UnauthenticatedException) {
      return GraphqlErrorBuilder.newError(env)
          .errorType(ErrorType.UNAUTHORIZED)
          .message(ex.getMessage())
          .build();
    }

    if (ex instanceof BadUserInputException) {
      return GraphqlErrorBuilder.newError(env)
          .errorType(ErrorType.BAD_REQUEST)
          .message(ex.getMessage())
          .build();
    }

    return GraphqlErrorBuilder.newError(env)
        .errorType(ErrorType.INTERNAL_ERROR)
        .message("An Unexpected error occurred.")
        .build();

  }
}
