package com.crewhandshake.features.auth;

import com.crewhandshake.features.messaging.service.SmsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "app.otp.phone-rate-limit-max=100",
    "app.otp.phone-rate-limit-window=PT15M",
    "app.otp.ip-rate-limit-max=100",
    "app.otp.ip-rate-limit-window=PT15M"
})
@AutoConfigureMockMvc
class OtpStartSmsFailureTest {
  @Autowired
  private MockMvc mockMvc;

  @Test
  void returnsSafeFailureWhenSmsProviderFails() throws Exception {
    mockMvc.perform(post("/api/v1/auth/otp/start")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"phone\": \"+14155559999\"}"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.errorCode").value("UNKNOWN"))
        .andExpect(jsonPath("$.message").value("Unable to send verification code"));
  }

  @TestConfiguration
  static class FailingSmsConfig {
    @Bean
    @Primary
    SmsProvider failingSmsProvider() {
      return (phoneE164, code) -> {
        throw new RuntimeException("provider failed");
      };
    }
  }
}
