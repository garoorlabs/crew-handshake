package com.crewhandshake.features.admin.api;

import java.util.UUID;

public record ForemanResponse(
    UUID membershipId,
    String displayName,
    String phoneE164,
    boolean active
) {}
