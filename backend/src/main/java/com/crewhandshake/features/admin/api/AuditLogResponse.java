package com.crewhandshake.features.admin.api;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
    UUID auditId,
    String actionType,
    String entityType,
    UUID entityId,
    String actorName,
    Instant createdAt,
    String detailsJson
) {}
