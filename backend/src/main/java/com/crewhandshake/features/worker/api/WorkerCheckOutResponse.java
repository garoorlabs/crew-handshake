package com.crewhandshake.features.worker.api;

import java.time.Instant;

public record WorkerCheckOutResponse(
    Instant checkOutAt
) {}
