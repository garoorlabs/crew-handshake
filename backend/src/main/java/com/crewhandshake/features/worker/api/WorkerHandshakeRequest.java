package com.crewhandshake.features.worker.api;

import com.crewhandshake.features.foreman.persistence.HandshakeStatus;
import jakarta.validation.constraints.NotNull;

public record WorkerHandshakeRequest(
    @NotNull(message = "Status is required")
    HandshakeStatus status,
    Integer lateEtaMinutes
) {}
