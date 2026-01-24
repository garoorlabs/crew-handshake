package com.crewhandshake.features.foreman.api;

import com.crewhandshake.features.foreman.persistence.CrewCallStatus;
import java.time.Instant;
import java.util.UUID;

public record CrewCallSummaryResponse(
    UUID crewCallId,
    String siteName,
    Instant startAt,
    String meetPoint,
    String sentByName,
    CrewCallStatus status
) {}
