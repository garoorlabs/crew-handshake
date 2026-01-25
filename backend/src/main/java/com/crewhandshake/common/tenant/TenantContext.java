package com.crewhandshake.common.tenant;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.security.AuthSession;
import com.crewhandshake.common.security.MembershipRole;
import com.crewhandshake.common.security.SessionService;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {
  private final SessionService sessionService;

  public TenantContext(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  public AuthSession requireSession() {
    return sessionService.requireSession();
  }

  public UUID requireCompanyId() {
    AuthSession session = requireSession();
    if (session.activeCompanyId() == null) {
      throw new ApiException(ApiErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "No active company");
    }
    return session.activeCompanyId();
  }

  public UUID requireMembershipId() {
    AuthSession session = requireSession();
    if (session.activeMembershipId() == null) {
      throw new ApiException(ApiErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "No active membership");
    }
    return session.activeMembershipId();
  }

  public Set<MembershipRole> roles() {
    AuthSession session = requireSession();
    return session.activeRoles() == null ? Set.of() : session.activeRoles();
  }

  public boolean isAdmin() {
    return roles().contains(MembershipRole.ADMIN);
  }

  public boolean isForeman() {
    return roles().contains(MembershipRole.FOREMAN);
  }

  public void requireAdmin() {
    requireCompanyId();
    requireMembershipId();
    if (!isAdmin()) {
      throw new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Not permitted");
    }
  }

  public void requireForemanOrAdmin() {
    requireCompanyId();
    requireMembershipId();
    if (!(isAdmin() || isForeman())) {
      throw new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Not permitted");
    }
  }
}
