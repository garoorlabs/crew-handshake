package com.crewhandshake.features.foreman.api;

import com.crewhandshake.features.foreman.persistence.CrewCallStatus;
import java.util.List;
import java.util.UUID;

public record CrewCallResponse(
    UUID crewCallId,
    CrewCallStatus status,
    List<CrewCallRecipientStatus> recipients
) {}
