package com.crewhandshake.features.admin.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SiteUpdateRequest(
    @NotNull(message = "Site id is required")
    UUID siteId,
    @NotBlank(message = "Name is required")
    String name,
    String address,
    String notes,
    Boolean active
) {}
