package com.crewhandshake.common.security;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SessionService {
  public static final String SESSION_KEY = "AUTH_SESSION";

  private final HttpServletRequest request;

  public SessionService(HttpServletRequest request) {
    this.request = request;
  }

  public Optional<AuthSession> getSession() {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return Optional.empty();
    }
    Object value = session.getAttribute(SESSION_KEY);
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
    HttpSession httpSession = request.getSession(true);
    httpSession.setAttribute(SESSION_KEY, session);
  }

  public void clearSession() {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    org.springframework.security.core.context.SecurityContextHolder.clearContext();
  }
}
