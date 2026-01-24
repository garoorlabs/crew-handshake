package com.crewhandshake.common.time;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class TimeParser {
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  public Instant parseInstant(String value, String fieldName) {
    try {
      return Instant.parse(value);
    } catch (DateTimeParseException ex) {
      throw new ApiException(
          ApiErrorCode.VALIDATION_ERROR,
          HttpStatus.BAD_REQUEST,
          "Invalid timestamp",
          Map.of(fieldName, "Invalid timestamp")
      );
    }
  }

  public LocalDate parseDate(String value, String fieldName) {
    try {
      return LocalDate.parse(value);
    } catch (DateTimeParseException ex) {
      throw new ApiException(
          ApiErrorCode.VALIDATION_ERROR,
          HttpStatus.BAD_REQUEST,
          "Invalid date",
          Map.of(fieldName, "Invalid date")
      );
    }
  }

  public LocalTime parseLocalTime(String value, String fieldName) {
    try {
      return LocalTime.parse(value, TIME_FORMAT);
    } catch (DateTimeParseException ex) {
      throw new ApiException(
          ApiErrorCode.VALIDATION_ERROR,
          HttpStatus.BAD_REQUEST,
          "Invalid time",
          Map.of(fieldName, "Invalid time")
      );
    }
  }
}
