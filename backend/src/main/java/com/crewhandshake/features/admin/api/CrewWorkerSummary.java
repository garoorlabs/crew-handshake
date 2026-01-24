package com.crewhandshake.features.admin.api;

import java.util.UUID;

public record CrewWorkerSummary(
    UUID membershipId,
    String displayName
) {}
