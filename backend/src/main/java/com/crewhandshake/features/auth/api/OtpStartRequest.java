package com.crewhandshake.features.auth.api;

import jakarta.validation.constraints.NotBlank;

public record OtpStartRequest(
    @NotBlank(message = "Phone is required")
    String phone
) {}
