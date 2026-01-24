package com.crewhandshake.features.foreman.api;

import com.crewhandshake.features.foreman.persistence.ExceptionResolutionAction;
import jakarta.validation.constraints.NotNull;

public record ExceptionResolveRequest(
    @NotNull(message = "Resolution action is required")
    ExceptionResolutionAction action,
    String checkInAt,
    String checkOutAt,
    String reason,
    String note
) {}
