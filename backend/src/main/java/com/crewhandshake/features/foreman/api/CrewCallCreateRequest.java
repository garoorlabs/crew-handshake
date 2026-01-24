package com.crewhandshake.features.foreman.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CrewCallCreateRequest(
    @NotNull(message = "Crew is required")
    UUID crewId,
    @NotNull(message = "Site is required")
    UUID siteId,
    @NotBlank(message = "Start time is required")
    String startAt,
    @NotBlank(message = "Meet point is required")
    String meetPoint
) {}
