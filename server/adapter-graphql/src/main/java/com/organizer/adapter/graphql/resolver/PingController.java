package com.organizer.adapter.graphql.resolver;

import com.organizer.application.PingUseCase;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class PingController {

  public final PingUseCase pingUseCase;

  public PingController(PingUseCase pingUseCase) {
    this.pingUseCase = pingUseCase;
  }

  @QueryMapping
  public String ping() {
    return pingUseCase.ping();
  }
}
