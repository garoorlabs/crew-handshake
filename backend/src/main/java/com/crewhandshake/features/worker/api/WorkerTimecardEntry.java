package com.crewhandshake.features.worker.api;

import com.crewhandshake.features.foreman.persistence.TimeEntryStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record WorkerTimecardEntry(
    UUID timeEntryId,
    LocalDate workDate,
    String crewName,
    String siteName,
    Instant checkInAt,
    Instant checkOutAt,
    TimeEntryStatus status,
    boolean edited,
    String editReason,
    String reviewStatus
) {}
