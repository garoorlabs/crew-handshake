package com.crewhandshake.features.foreman.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CrewCallUpdateRequest(
    @NotNull(message = "Site is required")
    UUID siteId,
    @NotBlank(message = "Start time is required")
    String startAt,
    @NotBlank(message = "Meet point is required")
    String meetPoint
) {}
