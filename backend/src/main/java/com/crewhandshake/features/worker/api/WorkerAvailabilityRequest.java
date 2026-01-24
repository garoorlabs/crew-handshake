package com.crewhandshake.features.worker.api;

import com.crewhandshake.features.foreman.persistence.AvailabilityAfter;
import jakarta.validation.constraints.NotNull;

public record WorkerAvailabilityRequest(
    @NotNull(message = "Availability is required")
    AvailabilityAfter availabilityAfter,
    Boolean differentSiteOk,
    String note
) {}
