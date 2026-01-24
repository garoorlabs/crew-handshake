package com.crewhandshake.features.auth.api;

import jakarta.validation.constraints.NotBlank;

public record ActiveCompanyRequest(
    @NotBlank(message = "Company ID is required")
    String companyId
) {}
