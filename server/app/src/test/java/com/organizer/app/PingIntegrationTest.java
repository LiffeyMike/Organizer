package com.organizer.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureHttpGraphQlTester
class PingIntegrationTest {

  @Autowired
  HttpGraphQlTester graphQlTester;

  @Test
  void pingReturnsPong() {
    graphQlTester.document("{ ping }")
      .execute()
      .path("ping")
      .entity(String.class)
      .isEqualTo("pong");
  }
}
