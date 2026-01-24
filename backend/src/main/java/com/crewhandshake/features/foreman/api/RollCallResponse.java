package com.crewhandshake.features.foreman.api;

import java.util.UUID;

public record RollCallResponse(
    UUID crewId,
    String date,
    int updatedCount
) {}
