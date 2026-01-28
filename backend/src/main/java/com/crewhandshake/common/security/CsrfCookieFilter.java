package com.crewhandshake.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.util.OnCommittedResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CsrfCookieFilter extends OncePerRequestFilter {
  private final CookieCsrfTokenRepository csrfTokenRepository;

  public CsrfCookieFilter(CookieCsrfTokenRepository csrfTokenRepository) {
    this.csrfTokenRepository = csrfTokenRepository;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    CsrfToken token = csrfTokenRepository.loadToken(request);
    if (token == null) {
      token = csrfTokenRepository.generateToken(request);
    }
    final CsrfToken csrfToken = token;
    OnCommittedResponseWrapper wrappedResponse = new OnCommittedResponseWrapper(response) {
      @Override
      protected void onResponseCommitted() {
        csrfTokenRepository.saveToken(csrfToken, request, response);
      }
    };
    filterChain.doFilter(request, wrappedResponse);
  }
}
