package com.crewhandshake.features.foreman.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record RollCallRequest(
    @NotNull(message = "Crew is required")
    UUID crewId,
    @NotBlank(message = "Date is required")
    String date,
    String recordedAt,
    @Valid
    List<RollCallEntry> entries
) {}
