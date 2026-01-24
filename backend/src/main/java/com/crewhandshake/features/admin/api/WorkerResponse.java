package com.crewhandshake.features.admin.api;

import java.util.UUID;

public record WorkerResponse(
    UUID membershipId,
    String displayName,
    String phoneE164,
    String preferredLanguage,
    boolean active,
    UUID crewId,
    String crewName
) {}
