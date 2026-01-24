package com.crewhandshake;

import com.crewhandshake.config.AppProperties;
import com.crewhandshake.config.OtpProperties;
import com.crewhandshake.config.SeedProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, SeedProperties.class, OtpProperties.class})
public class CrewHandshakeApplication {
  public static void main(String[] args) {
    SpringApplication.run(CrewHandshakeApplication.class, args);
  }
}
