package com.crewhandshake.features.foreman.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RollCallEntry(
    @NotNull(message = "Worker is required")
    UUID workerMembershipId,
    @NotNull(message = "Status is required")
    RollCallStatus status
) {}
