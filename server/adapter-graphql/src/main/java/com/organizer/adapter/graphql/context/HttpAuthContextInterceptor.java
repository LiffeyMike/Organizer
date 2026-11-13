package com.organizer.adapter.graphql.context;

import com.organizer.adapter.security.CurrentPrincipal;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class HttpAuthContextInterceptor implements WebGraphQlInterceptor {

  @Override
  public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
    CurrentPrincipal.get()
        .ifPresent(principal ->
            request.configureExecutionInput((input, builder) ->
                builder
                    .graphQLContext(ctx -> ctx.put(AuthContext.PRINCIPAL_KEY, principal))
                    .build()));
    return chain.next(request);
  }
}
