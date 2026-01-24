package com.crewhandshake.features.auth.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PhoneNormalizer {
  private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");

  public String normalize(String raw) {
    if (raw == null) {
      throw new ApiException(
          ApiErrorCode.VALIDATION_ERROR,
          HttpStatus.BAD_REQUEST,
          "Phone is required",
          java.util.Map.of("phone", "Phone is required")
      );
    }
    String trimmed = raw.trim();
    if (trimmed.isEmpty()) {
      throw new ApiException(
          ApiErrorCode.VALIDATION_ERROR,
          HttpStatus.BAD_REQUEST,
          "Phone is required",
          java.util.Map.of("phone", "Phone is required")
      );
    }
    if (trimmed.startsWith("+")) {
      if (E164.matcher(trimmed).matches()) {
        return trimmed;
      }
      throw new ApiException(
          ApiErrorCode.VALIDATION_ERROR,
          HttpStatus.BAD_REQUEST,
          "Invalid phone format",
          java.util.Map.of("phone", "Invalid phone format")
      );
    }
    String digits = trimmed.replaceAll("\\D", "");
    if (digits.length() == 10) {
      return "+1" + digits;
    }
    if (digits.length() == 11 && digits.startsWith("1")) {
      return "+" + digits;
    }
    throw new ApiException(
        ApiErrorCode.VALIDATION_ERROR,
        HttpStatus.BAD_REQUEST,
        "Invalid phone format",
        java.util.Map.of("phone", "Invalid phone format")
    );
  }
}
