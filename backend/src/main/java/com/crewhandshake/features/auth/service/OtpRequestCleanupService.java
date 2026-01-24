package com.crewhandshake.features.auth.service;

import com.crewhandshake.config.OtpProperties;
import com.crewhandshake.features.auth.persistence.OtpRequestAttemptRepository;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpRequestCleanupService {
  private static final Duration DEFAULT_RETENTION = Duration.ofHours(24);
  private static final Logger logger = LoggerFactory.getLogger(OtpRequestCleanupService.class);

  private final OtpRequestAttemptRepository otpRequestAttemptRepository;
  private final OtpProperties otpProperties;

  public OtpRequestCleanupService(OtpRequestAttemptRepository otpRequestAttemptRepository,
                                  OtpProperties otpProperties) {
    this.otpRequestAttemptRepository = otpRequestAttemptRepository;
    this.otpProperties = otpProperties;
  }

  @Scheduled(cron = "0 0 * * * *")
  @Transactional
  public void runCleanupJob() {
    cleanupOldAttempts(Instant.now());
  }

  @Transactional
  void cleanupOldAttempts(Instant now) {
    Duration retention = resolveRetention(otpProperties.attemptRetention());
    Instant cutoff = now.minus(retention);
    int deleted = otpRequestAttemptRepository.deleteByCreatedAtBefore(cutoff);
    if (deleted > 0) {
      logger.info("OTP request cleanup removed {} attempts", deleted);
    }
  }

  private Duration resolveRetention(Duration value) {
    if (value == null || value.isZero() || value.isNegative()) {
      return DEFAULT_RETENTION;
    }
    return value;
  }
}
