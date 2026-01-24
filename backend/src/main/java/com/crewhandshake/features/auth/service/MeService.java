package com.crewhandshake.features.auth.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.security.AuthSession;
import com.crewhandshake.common.security.MembershipRole;
import com.crewhandshake.common.security.SessionService;
import com.crewhandshake.features.auth.api.MeMembership;
import com.crewhandshake.features.auth.api.MeResponse;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import com.crewhandshake.features.auth.persistence.MembershipRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeService {
  private final SessionService sessionService;
  private final MembershipRepository membershipRepository;

  public MeService(SessionService sessionService, MembershipRepository membershipRepository) {
    this.sessionService = sessionService;
    this.membershipRepository = membershipRepository;
  }

  @Transactional(readOnly = true)
  public MeResponse getMe() {
    AuthSession session = sessionService.requireSession();
    List<MembershipEntity> memberships = membershipRepository.findByIdentityId(session.identityId());
    List<MeMembership> membershipDtos = memberships.stream()
        .map(membership -> new MeMembership(
            membership.getId(),
            membership.getCompany().getId(),
            membership.getCompany().getName(),
            membership.getRoles()
        ))
        .collect(Collectors.toList());
    return new MeResponse(
        session.identityId(),
        session.phoneE164(),
        session.activeCompanyId(),
        session.activeMembershipId(),
        session.activeRoles(),
        membershipDtos
    );
  }

  @Transactional
  public MeResponse setActiveCompany(UUID companyId) {
    AuthSession session = sessionService.requireSession();
    MembershipEntity membership = membershipRepository
        .findByIdentityIdAndCompanyId(session.identityId(), companyId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Not permitted"));

    Set<MembershipRole> roles = membership.getRoles();
    AuthSession updated = new AuthSession(
        session.identityId(),
        session.phoneE164(),
        membership.getCompany().getId(),
        membership.getId(),
        roles
    );
    sessionService.saveSession(updated);
    return getMe();
  }
}
