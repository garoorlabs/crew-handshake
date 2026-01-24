package com.crewhandshake.features.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "app.otp.phone-rate-limit-max=2",
    "app.otp.phone-rate-limit-window=PT15M",
    "app.otp.ip-rate-limit-max=100",
    "app.otp.ip-rate-limit-window=PT15M"
})
@AutoConfigureMockMvc
class OtpStartPhoneRateLimitTest {
  @Autowired
  private MockMvc mockMvc;

  @Test
  void rejectsInvalidPhoneWithFieldError() throws Exception {
    mockMvc.perform(post("/api/v1/auth/otp/start")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"phone\": \"abc\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.fieldErrors.phone").exists());
  }

  @Test
  void rateLimitsByPhone() throws Exception {
    String phone = "+14155550000";

    for (int i = 0; i < 2; i++) {
      mockMvc.perform(post("/api/v1/auth/otp/start")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"phone\": \"" + phone + "\"}"))
          .andExpect(status().isOk());
    }

    mockMvc.perform(post("/api/v1/auth/otp/start")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"phone\": \"" + phone + "\"}"))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.errorCode").value("RATE_LIMITED"));
  }
}
