package com.crewhandshake.features.auth.service;

import com.crewhandshake.features.auth.persistence.OtpRequestAttemptEntity;
import com.crewhandshake.features.auth.persistence.OtpRequestAttemptRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.otp.attempt-retention=PT1H",
    "spring.task.scheduling.enabled=false"
})
class OtpRequestCleanupServiceTest {
  @Autowired
  private OtpRequestCleanupService cleanupService;

  @Autowired
  private OtpRequestAttemptRepository otpRequestAttemptRepository;

  @BeforeEach
  void clearAttempts() {
    otpRequestAttemptRepository.deleteAll();
  }

  @Test
  void deletesAttemptsOlderThanRetentionWindow() {
    Instant now = Instant.parse("2026-01-01T00:00:00Z");
    otpRequestAttemptRepository.save(new OtpRequestAttemptEntity("+14155550000", "10.0.0.1", now.minus(Duration.ofHours(2))));
    otpRequestAttemptRepository.save(new OtpRequestAttemptEntity("+14155550001", "10.0.0.2", now.minus(Duration.ofMinutes(30))));

    cleanupService.cleanupOldAttempts(now);

    assertThat(otpRequestAttemptRepository.count()).isEqualTo(1);
  }

  @Test
  void keepsAttemptsWithinRetentionWindow() {
    Instant now = Instant.parse("2026-01-01T00:00:00Z");
    otpRequestAttemptRepository.save(new OtpRequestAttemptEntity("+14155550002", "10.0.0.3", now.minus(Duration.ofMinutes(10))));

    cleanupService.cleanupOldAttempts(now);

    assertThat(otpRequestAttemptRepository.count()).isEqualTo(1);
  }
}
