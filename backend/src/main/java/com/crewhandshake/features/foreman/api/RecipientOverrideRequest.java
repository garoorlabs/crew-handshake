package com.crewhandshake.features.foreman.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RecipientOverrideRequest(
    @NotNull(message = "Crew call is required")
    UUID crewCallId,
    @NotNull(message = "Worker is required")
    UUID workerMembershipId,
    @NotNull(message = "Site is required")
    UUID siteId,
    @NotBlank(message = "Start time is required")
    String startAt,
    @NotBlank(message = "Meet point is required")
    String meetPoint
) {}
