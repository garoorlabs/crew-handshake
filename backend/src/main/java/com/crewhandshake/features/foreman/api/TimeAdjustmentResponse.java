package com.crewhandshake.features.foreman.api;

import com.crewhandshake.features.foreman.persistence.TimeEntryStatus;
import java.time.Instant;
import java.util.UUID;

public record TimeAdjustmentResponse(
    UUID timeEntryId,
    Instant checkInAt,
    Instant checkOutAt,
    TimeEntryStatus status,
    boolean edited
) {}
