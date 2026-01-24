package com.crewhandshake.features.worker.api;

import java.time.Instant;
import java.util.UUID;

public record WorkerReviewRequestResponse(
    UUID reviewRequestId,
    String status,
    Instant createdAt
) {}
