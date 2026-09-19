package com.organizer.adapter.graphql.resolver;

import com.organizer.application.PingUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;

@GraphQlTest(PingController.class)
class PingControllerTest {
  @Autowired
  GraphQlTester graphQlTester;

  @MockitoBean
  PingUseCase pingUseCase;

  @Test
  void pingDelegatesToUseCase() {
    when(pingUseCase.ping()).thenReturn("pong");

    graphQlTester.document("{ ping }")
        .execute()
        .path("ping")
        .entity(String.class)
        .isEqualTo("pong");
  }
}
