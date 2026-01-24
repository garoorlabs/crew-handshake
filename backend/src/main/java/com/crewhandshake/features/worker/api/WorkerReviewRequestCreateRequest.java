package com.crewhandshake.features.worker.api;

import jakarta.validation.constraints.NotBlank;

public record WorkerReviewRequestCreateRequest(
    @NotBlank(message = "Work date is required")
    String workDate,
    @NotBlank(message = "Reason is required")
    String reason,
    String note
) {}
