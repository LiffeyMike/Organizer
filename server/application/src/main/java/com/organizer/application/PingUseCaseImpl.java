package com.organizer.application;

import org.springframework.stereotype.Service;

@Service
public class PingUseCaseImpl implements PingUseCase {
  @Override
  public String ping() {
    return "pong";
  }
}
