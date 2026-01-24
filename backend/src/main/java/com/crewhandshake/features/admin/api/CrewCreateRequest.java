package com.crewhandshake.features.admin.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CrewCreateRequest(
    @NotBlank(message = "Name is required")
    String name,
    @NotNull(message = "Foreman is required")
    UUID foremanMembershipId,
    List<UUID> workerMembershipIds
) {}
