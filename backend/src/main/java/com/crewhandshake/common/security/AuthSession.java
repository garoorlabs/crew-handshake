package com.crewhandshake.common.security;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

public record AuthSession(
    UUID identityId,
    String phoneE164,
    UUID activeCompanyId,
    UUID activeMembershipId,
    Set<MembershipRole> activeRoles
) implements Serializable {
}
