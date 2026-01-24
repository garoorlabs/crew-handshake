package com.crewhandshake.features.auth.api;

import com.crewhandshake.common.security.MembershipRole;
import java.util.Set;
import java.util.UUID;

public record MeMembership(
    UUID membershipId,
    UUID companyId,
    String companyName,
    Set<MembershipRole> roles
) {}
