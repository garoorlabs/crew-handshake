package com.crewhandshake.common.errors;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
  private final ApiErrorCode errorCode;
  private final HttpStatus status;
  private final Map<String, String> fieldErrors;

  public ApiException(ApiErrorCode errorCode, HttpStatus status, String message) {
    super(message);
    this.errorCode = errorCode;
    this.status = status;
    this.fieldErrors = null;
  }

  public ApiException(ApiErrorCode errorCode, HttpStatus status, String message, Map<String, String> fieldErrors) {
    super(message);
    this.errorCode = errorCode;
    this.status = status;
    this.fieldErrors = fieldErrors;
  }

  public ApiErrorCode getErrorCode() {
    return errorCode;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public Map<String, String> getFieldErrors() {
    return fieldErrors;
  }
}
