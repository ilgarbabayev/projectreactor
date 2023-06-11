package com.reactor.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
public class ProjectreactorApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProjectreactorApplication.class, args);
  }
}
