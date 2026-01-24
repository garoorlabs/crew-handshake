package com.crewhandshake.features.admin.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ForemanUpdateRequest(
    @NotNull(message = "Foreman id is required")
    UUID membershipId,
    @NotBlank(message = "Name is required")
    String displayName,
    Boolean active
) {}
