package com.organizer.adapter.security.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class MutableClock extends Clock {

  public static final Instant DEFAULT_START = Instant.parse("2026-01-01T00:00:00Z");

  private Instant now;

  public MutableClock() {
    this(DEFAULT_START);
  }

  public MutableClock(Instant start) {
    if (start.getNano() != 0) {
      throw new IllegalArgumentException("Start the clock on a whole second. Got: " + start);
    }
    this.now = start;
  }

  @Override
  public Instant instant() {
    return now;
  }

  @Override
  public ZoneId getZone() {
    return ZoneOffset.UTC;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return this;
  }

  public void advance(Duration duration) {
    now = now.plus(duration);
  }
}
