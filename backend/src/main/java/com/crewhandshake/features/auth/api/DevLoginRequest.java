package com.crewhandshake.features.auth.api;

import jakarta.validation.constraints.NotBlank;

public record DevLoginRequest(
    @NotBlank(message = "Phone is required")
    String phone
) {}
