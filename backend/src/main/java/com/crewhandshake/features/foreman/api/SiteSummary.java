package com.crewhandshake.features.foreman.api;

import java.util.UUID;

public record SiteSummary(
    UUID siteId,
    String name,
    String address
) {}
