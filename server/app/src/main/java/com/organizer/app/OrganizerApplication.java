package com.organizer.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = "com.organizer")
@EnableJpaRepositories(basePackages = "com.organizer.adapter.persistence")
@EntityScan(basePackages = "com.organizer.adapter.persistence")
public class OrganizerApplication {
  public static void main(String[] args) {
    SpringApplication.run(OrganizerApplication.class, args);
  }
}
