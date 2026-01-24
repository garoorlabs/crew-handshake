package com.crewhandshake.features.worker.api;

import java.time.Instant;

public record WorkerCheckInResponse(
    Instant checkInAt
) {}
