package com.crewhandshake.features.admin.api;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record WorkerCreateRequest(
    @NotBlank(message = "Name is required")
    String displayName,
    @NotBlank(message = "Phone is required")
    String phone,
    String preferredLanguage,
    UUID crewId,
    Boolean active
) {}
