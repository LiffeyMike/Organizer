package com.organizer.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.organizer")
public class OrganizerApplication {
  public static void main(String[] args) {
    SpringApplication.run(OrganizerApplication.class, args);
  }
}
