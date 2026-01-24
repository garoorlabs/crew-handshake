package com.crewhandshake.common.errors;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
    ApiErrorResponse response = new ApiErrorResponse(
        ex.getErrorCode().name(),
        ex.getMessage(),
        ex.getFieldErrors()
    );
    return ResponseEntity.status(ex.getStatus()).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(error.getField(), error.getDefaultMessage());
    }
    ApiErrorResponse response = new ApiErrorResponse(
        ApiErrorCode.VALIDATION_ERROR.name(),
        "Validation failed",
        fieldErrors
    );
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidPayload(HttpMessageNotReadableException ex) {
    ApiErrorResponse response = new ApiErrorResponse(
        ApiErrorCode.VALIDATION_ERROR.name(),
        "Invalid request payload",
        null
    );
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
    ApiErrorResponse response = new ApiErrorResponse(
        ApiErrorCode.FORBIDDEN.name(),
        "Not permitted",
        null
    );
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnknown(Exception ex) {
    ApiErrorResponse response = new ApiErrorResponse(
        ApiErrorCode.UNKNOWN.name(),
        "Something went wrong",
        null
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
