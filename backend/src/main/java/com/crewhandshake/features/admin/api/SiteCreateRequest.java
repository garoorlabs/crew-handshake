package com.crewhandshake.features.admin.api;

import jakarta.validation.constraints.NotBlank;

public record SiteCreateRequest(
    @NotBlank(message = "Name is required")
    String name,
    String address,
    String notes,
    Boolean active
) {}
