package com.crewhandshake.features.auth.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.security.AuthSession;
import com.crewhandshake.common.security.MembershipRole;
import com.crewhandshake.common.security.SessionService;
import com.crewhandshake.features.auth.api.MeResponse;
import com.crewhandshake.features.auth.persistence.IdentityEntity;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import com.crewhandshake.features.auth.persistence.MembershipRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final OtpService otpService;
  private final MembershipRepository membershipRepository;
  private final SessionService sessionService;
  private final MeService meService;

  public AuthService(OtpService otpService,
                     MembershipRepository membershipRepository,
                     SessionService sessionService,
                     MeService meService) {
    this.otpService = otpService;
    this.membershipRepository = membershipRepository;
    this.sessionService = sessionService;
    this.meService = meService;
  }

  public String startOtp(String phone, String ipAddress) {
    return otpService.startOtp(phone, ipAddress);
  }

  @Transactional
  public MeResponse verifyOtp(String phone, String code) {
    IdentityEntity identity = otpService.verifyOtp(phone, code);
    List<MembershipEntity> memberships = membershipRepository.findByIdentityId(identity.getId());
    if (memberships.isEmpty()) {
      throw new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "No active membership");
    }

    UUID activeCompanyId = null;
    UUID activeMembershipId = null;
    Set<MembershipRole> activeRoles = Set.of();
    if (memberships.size() == 1) {
      MembershipEntity membership = memberships.get(0);
      activeCompanyId = membership.getCompany().getId();
      activeMembershipId = membership.getId();
      activeRoles = membership.getRoles();
    }

    AuthSession session = new AuthSession(
        identity.getId(),
        identity.getPhoneE164(),
        activeCompanyId,
        activeMembershipId,
        activeRoles
    );
    sessionService.saveSession(session);

    return meService.getMe();
  }

  public void logout() {
    sessionService.clearSession();
  }
}
