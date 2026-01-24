package com.crewhandshake.features.auth.api;

import com.crewhandshake.features.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/otp/start")
  public OtpStartResponse startOtp(@Valid @RequestBody OtpStartRequest request, HttpServletRequest httpRequest) {
    String phoneE164 = authService.startOtp(request.phone(), resolveClientIp(httpRequest));
    return new OtpStartResponse(phoneE164);
  }

  @PostMapping("/otp/verify")
  public MeResponse verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
    return authService.verifyOtp(request.phone(), request.code());
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout() {
    authService.logout();
  }

  private String resolveClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      String[] parts = forwarded.split(",");
      if (parts.length > 0) {
        String first = parts[0].trim();
        if (!first.isEmpty()) {
          return first;
        }
      }
    }
    return request.getRemoteAddr();
  }
}
