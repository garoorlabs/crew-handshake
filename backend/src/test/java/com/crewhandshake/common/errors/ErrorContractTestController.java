package com.crewhandshake.common.errors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/test-errors")
public class ErrorContractTestController {
  @PostMapping("/validation")
  public String triggerValidation(@Valid @RequestBody ValidationPayload payload) {
    return "ok";
  }

  @GetMapping("/unauthorized")
  public void triggerUnauthorized() {
    throw new ApiException(ApiErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Unauthorized", null);
  }

  @GetMapping("/forbidden")
  public void triggerForbidden() {
    throw new org.springframework.security.access.AccessDeniedException("Not permitted");
  }

  @GetMapping("/unknown")
  public void triggerUnknown() {
    throw new RuntimeException("boom");
  }

  public record ValidationPayload(@NotBlank String name) {}
}
