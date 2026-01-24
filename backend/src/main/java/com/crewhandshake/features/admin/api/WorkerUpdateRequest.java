package com.crewhandshake.features.admin.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WorkerUpdateRequest(
    @NotNull(message = "Worker id is required")
    UUID membershipId,
    @NotBlank(message = "Name is required")
    String displayName,
    String preferredLanguage,
    UUID crewId,
    Boolean active
) {}
