package com.organizer.app;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.organizer.application.PingUseCase;

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
