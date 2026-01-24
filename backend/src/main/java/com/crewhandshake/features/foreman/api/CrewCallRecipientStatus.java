package com.crewhandshake.features.foreman.api;

import com.crewhandshake.features.foreman.persistence.RecipientSendStatus;
import java.util.UUID;

public record CrewCallRecipientStatus(
    UUID workerMembershipId,
    String workerName,
    String phoneE164,
    RecipientSendStatus sendStatus,
    String sendError
) {}
