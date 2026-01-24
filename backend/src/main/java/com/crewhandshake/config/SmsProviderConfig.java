package com.crewhandshake.config;

import com.crewhandshake.features.messaging.service.LogSmsProvider;
import com.crewhandshake.features.messaging.service.NoopSmsProvider;
import com.crewhandshake.features.messaging.service.SmsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsProviderConfig {
  @Bean
  public SmsProvider smsProvider(AppProperties appProperties) {
    String provider = appProperties.smsProvider() == null ? "noop" : appProperties.smsProvider().trim();
    if (provider.equalsIgnoreCase("log")) {
      return new LogSmsProvider();
    }
    return new NoopSmsProvider();
  }
}
