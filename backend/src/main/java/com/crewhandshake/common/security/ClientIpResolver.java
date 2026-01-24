package com.crewhandshake.common.security;

import com.crewhandshake.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

@Component
public class ClientIpResolver {
  private final List<IpAddressMatcher> trustedProxyMatchers;

  public ClientIpResolver(AppProperties appProperties) {
    List<String> trustedProxies = appProperties.trustedProxies();
    if (trustedProxies == null) {
      trustedProxies = List.of();
    }
    this.trustedProxyMatchers = trustedProxies.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .distinct()
        .map(IpAddressMatcher::new)
        .toList();
  }

  public String resolveClientIp(HttpServletRequest request) {
    String remoteAddr = request.getRemoteAddr();
    if (!isFromTrustedProxy(remoteAddr)) {
      return remoteAddr;
    }

    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      String[] parts = forwarded.split(",");
      for (String part : parts) {
        String candidate = part.trim();
        if (!candidate.isEmpty()) {
          return candidate;
        }
      }
    }

    return remoteAddr;
  }

  private boolean isFromTrustedProxy(String remoteAddr) {
    if (trustedProxyMatchers.isEmpty() || remoteAddr == null || remoteAddr.isBlank()) {
      return false;
    }
    return trustedProxyMatchers.stream().anyMatch(matcher -> matcher.matches(remoteAddr));
  }
}
