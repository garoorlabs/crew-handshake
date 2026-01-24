package com.crewhandshake.features.auth.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.features.auth.persistence.IdentityEntity;
import com.crewhandshake.features.auth.persistence.IdentityRepository;
import com.crewhandshake.features.auth.persistence.OtpCodeEntity;
import com.crewhandshake.features.auth.persistence.OtpCodeRepository;
import com.crewhandshake.features.messaging.service.SmsProvider;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService {
  private static final Duration OTP_TTL = Duration.ofMinutes(10);
  private static final Duration OTP_COOLDOWN = Duration.ofSeconds(60);
  private static final int OTP_LENGTH = 6;
  private static final int MAX_ATTEMPTS = 5;

  private final IdentityRepository identityRepository;
  private final OtpCodeRepository otpCodeRepository;
  private final SmsProvider smsProvider;
  private final PhoneNormalizer phoneNormalizer;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final SecureRandom random = new SecureRandom();

  public OtpService(IdentityRepository identityRepository,
                    OtpCodeRepository otpCodeRepository,
                    SmsProvider smsProvider,
                    PhoneNormalizer phoneNormalizer) {
    this.identityRepository = identityRepository;
    this.otpCodeRepository = otpCodeRepository;
    this.smsProvider = smsProvider;
    this.phoneNormalizer = phoneNormalizer;
  }

  @Transactional
  public String startOtp(String phoneRaw) {
    String phoneE164 = phoneNormalizer.normalize(phoneRaw);
    IdentityEntity identity = identityRepository.findByPhoneE164(phoneE164)
        .orElseGet(() -> identityRepository.save(new IdentityEntity(phoneE164)));

    otpCodeRepository.findTopByIdentityIdOrderByCreatedAtDesc(identity.getId())
        .ifPresent(existing -> {
          Instant now = Instant.now();
          if (existing.getCreatedAt().plus(OTP_COOLDOWN).isAfter(now)) {
            throw new ApiException(ApiErrorCode.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, "Try again soon");
          }
        });

    String code = generateCode();
    String codeHash = passwordEncoder.encode(code);
    Instant now = Instant.now();
    OtpCodeEntity otp = new OtpCodeEntity(identity, codeHash, now, now.plus(OTP_TTL), MAX_ATTEMPTS);
    otpCodeRepository.save(otp);

    smsProvider.sendOtp(phoneE164, code);
    return phoneE164;
  }

  @Transactional
  public IdentityEntity verifyOtp(String phoneRaw, String codeRaw) {
    String phoneE164 = phoneNormalizer.normalize(phoneRaw);
    if (codeRaw == null || codeRaw.trim().length() != OTP_LENGTH) {
      throw new ApiException(ApiErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid code");
    }
    IdentityEntity identity = identityRepository.findByPhoneE164(phoneE164)
        .orElseThrow(() -> new ApiException(ApiErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid code"));

    OtpCodeEntity otp = otpCodeRepository.findTopByIdentityIdOrderByCreatedAtDesc(identity.getId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid code"));

    Instant now = Instant.now();
    if (otp.getConsumedAt() != null || otp.getExpiresAt().isBefore(now)) {
      throw new ApiException(ApiErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid code");
    }

    if (otp.getAttempts() >= otp.getMaxAttempts()) {
      throw new ApiException(ApiErrorCode.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, "Too many attempts");
    }

    if (!passwordEncoder.matches(codeRaw.trim(), otp.getCodeHash())) {
      otp.incrementAttempts();
      otpCodeRepository.save(otp);
      if (otp.getAttempts() >= otp.getMaxAttempts()) {
        throw new ApiException(ApiErrorCode.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, "Too many attempts");
      }
      throw new ApiException(ApiErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid code");
    }

    otp.markConsumed(now);
    otpCodeRepository.save(otp);
    return identity;
  }

  private String generateCode() {
    int min = (int) Math.pow(10, OTP_LENGTH - 1);
    int max = (int) Math.pow(10, OTP_LENGTH) - 1;
    int value = random.nextInt(max - min + 1) + min;
    return String.valueOf(value);
  }
}
