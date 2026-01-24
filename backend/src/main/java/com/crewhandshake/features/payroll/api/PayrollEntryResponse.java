package com.crewhandshake.features.payroll.api;

import com.crewhandshake.features.foreman.persistence.TimeEntryStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PayrollEntryResponse(
    UUID timeEntryId,
    UUID workerMembershipId,
    String workerName,
    LocalDate workDate,
    Instant checkInAt,
    Instant checkOutAt,
    TimeEntryStatus status,
    boolean edited
) {}
