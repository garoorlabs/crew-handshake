package com.crewhandshake.features.foreman.api;

import com.crewhandshake.features.foreman.persistence.ExceptionStatus;
import com.crewhandshake.features.foreman.persistence.ExceptionType;
import java.time.Instant;
import java.util.UUID;

public record ExceptionResponse(
    UUID exceptionId,
    ExceptionType type,
    ExceptionStatus status,
    UUID crewId,
    String crewName,
    UUID workerMembershipId,
    String workerName,
    UUID timeEntryId,
    UUID reviewRequestId,
    Instant checkInAt,
    Instant checkOutAt,
    String reviewReason,
    String reviewNote
) {}
