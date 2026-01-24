package com.crewhandshake.features.admin.api;

import jakarta.validation.constraints.NotBlank;

public record ForemanCreateRequest(
    @NotBlank(message = "Name is required")
    String displayName,
    @NotBlank(message = "Phone is required")
    String phone,
    Boolean active
) {}
