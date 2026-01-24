package com.crewhandshake.features.admin.api;

import java.util.List;
import java.util.UUID;

public record CrewResponse(
    UUID crewId,
    String name,
    UUID foremanMembershipId,
    String foremanName,
    int workerCount,
    List<CrewWorkerSummary> workers
) {}
