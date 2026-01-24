package com.crewhandshake.features.auth.api;

import com.crewhandshake.common.security.MembershipRole;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record MeResponse(
    UUID identityId,
    String phoneE164,
    UUID activeCompanyId,
    UUID activeMembershipId,
    Set<MembershipRole> activeRoles,
    List<MeMembership> memberships
) {}
