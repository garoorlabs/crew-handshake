package com.crewhandshake.features.admin.api;

import java.util.UUID;

public record SiteResponse(
    UUID siteId,
    String name,
    String address,
    String notes,
    boolean active
) {}
