package com.crewhandshake.common.security;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SessionService {
  public static final String SESSION_KEY = "AUTH_SESSION";

  private final HttpSession httpSession;

  public SessionService(HttpSession httpSession) {
    this.httpSession = httpSession;
  }

  public Optional<AuthSession> getSession() {
    Object value = httpSession.getAttribute(SESSION_KEY);
    if (value instanceof AuthSession authSession) {
      return Optional.of(authSession);
    }
    return Optional.empty();
  }

  public AuthSession requireSession() {
    return getSession().orElseThrow(() -> new ApiException(
        ApiErrorCode.UNAUTHORIZED,
        HttpStatus.UNAUTHORIZED,
        "Unauthorized"
    ));
  }

  public void saveSession(AuthSession session) {
    httpSession.setAttribute(SESSION_KEY, session);
  }

  public void clearSession() {
    httpSession.invalidate();
    org.springframework.security.core.context.SecurityContextHolder.clearContext();
  }
}
