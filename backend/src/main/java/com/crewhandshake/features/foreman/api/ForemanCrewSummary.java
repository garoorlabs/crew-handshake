package com.crewhandshake.features.foreman.api;

import java.util.UUID;

public record ForemanCrewSummary(
    UUID crewId,
    String name
) {}
