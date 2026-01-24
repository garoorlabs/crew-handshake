package com.crewhandshake.features.auth.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.config.OtpProperties;
import com.crewhandshake.features.auth.persistence.OtpRequestAttemptEntity;
import com.crewhandshake.features.auth.persistence.OtpRequestAttemptRepository;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpRateLimiter {
  private static final int DEFAULT_PHONE_MAX = 5;
  private static final int DEFAULT_IP_MAX = 20;
  private static final Duration DEFAULT_WINDOW = Duration.ofMinutes(15);

  private final OtpRequestAttemptRepository otpRequestAttemptRepository;
  private final OtpProperties otpProperties;

  public OtpRateLimiter(OtpRequestAttemptRepository otpRequestAttemptRepository,
                        OtpProperties otpProperties) {
    this.otpRequestAttemptRepository = otpRequestAttemptRepository;
    this.otpProperties = otpProperties;
  }

  @Transactional
  public void checkAndRecord(String phoneE164, String ipAddressRaw) {
    String ipAddress = (ipAddressRaw == null || ipAddressRaw.isBlank()) ? "unknown" : ipAddressRaw.trim();
    Instant now = Instant.now();

    int phoneMax = resolveMax(otpProperties.phoneRateLimitMax(), DEFAULT_PHONE_MAX);
    Duration phoneWindow = resolveWindow(otpProperties.phoneRateLimitWindow(), DEFAULT_WINDOW);
    int phoneCount = otpRequestAttemptRepository.countByPhoneE164AndCreatedAtAfter(
        phoneE164,
        now.minus(phoneWindow)
    );
    if (phoneCount >= phoneMax) {
      throw new ApiException(ApiErrorCode.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, "Try again later");
    }

    int ipMax = resolveMax(otpProperties.ipRateLimitMax(), DEFAULT_IP_MAX);
    Duration ipWindow = resolveWindow(otpProperties.ipRateLimitWindow(), DEFAULT_WINDOW);
    int ipCount = otpRequestAttemptRepository.countByIpAddressAndCreatedAtAfter(
        ipAddress,
        now.minus(ipWindow)
    );
    if (ipCount >= ipMax) {
      throw new ApiException(ApiErrorCode.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, "Try again later");
    }

    otpRequestAttemptRepository.save(new OtpRequestAttemptEntity(phoneE164, ipAddress, now));
  }

  private int resolveMax(Integer value, int fallback) {
    if (value == null || value <= 0) {
      return fallback;
    }
    return value;
  }

  private Duration resolveWindow(Duration value, Duration fallback) {
    if (value == null || value.isZero() || value.isNegative()) {
      return fallback;
    }
    return value;
  }
}
