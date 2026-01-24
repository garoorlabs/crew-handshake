package com.crewhandshake.features.foreman.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TimeAdjustmentRequest(
    @NotNull(message = "Time entry is required")
    UUID timeEntryId,
    @NotBlank(message = "Check-in time is required")
    String checkInAt,
    @NotBlank(message = "Check-out time is required")
    String checkOutAt,
    @NotBlank(message = "Reason is required")
    String reason,
    String note
) {}
