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
    "app.otp.phone-rate-limit-max=100",
    "app.otp.phone-rate-limit-window=PT15M",
    "app.otp.ip-rate-limit-max=2",
    "app.otp.ip-rate-limit-window=PT15M"
})
@AutoConfigureMockMvc
class OtpStartIpRateLimitTest {
  @Autowired
  private MockMvc mockMvc;

  @Test
  void rateLimitsByIp() throws Exception {
    String ip = "10.0.0.42";

    mockMvc.perform(post("/api/v1/auth/otp/start")
        .with(request -> {
          request.setRemoteAddr(ip);
          return request;
        })
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"phone\": \"+14155550101\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/auth/otp/start")
        .with(request -> {
          request.setRemoteAddr(ip);
          return request;
        })
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"phone\": \"+14155550102\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/auth/otp/start")
        .with(request -> {
          request.setRemoteAddr(ip);
          return request;
        })
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"phone\": \"+14155550103\"}"))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.errorCode").value("RATE_LIMITED"));
  }
}
