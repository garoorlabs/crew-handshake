package com.crewhandshake.features.foreman.api;

import com.crewhandshake.features.foreman.persistence.HandshakeStatus;
import java.time.Instant;
import java.util.UUID;

public record TodayWorkerStatus(
    UUID membershipId,
    String displayName,
    String phoneE164,
    HandshakeStatus handshakeStatus,
    Integer lateEtaMinutes,
    Instant checkInAt,
    Instant checkOutAt,
    boolean hasException,
    UUID timeEntryId
) {}
