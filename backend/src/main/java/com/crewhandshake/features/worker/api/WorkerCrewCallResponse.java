package com.crewhandshake.features.worker.api;

import com.crewhandshake.features.foreman.persistence.AvailabilityAfter;
import com.crewhandshake.features.foreman.persistence.HandshakeStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkerCrewCallResponse(
    UUID crewCallId,
    String companyName,
    String crewName,
    String siteName,
    String siteAddress,
    Instant startAt,
    String meetPoint,
    String senderName,
    HandshakeStatus handshakeStatus,
    Integer lateEtaMinutes,
    AvailabilityAfter availabilityAfter,
    Boolean availabilityDifferentSiteOk,
    String availabilityNote,
    Instant checkInAt,
    Instant checkOutAt,
    WorkerAction primaryAction,
    List<WorkerAction> availableActions,
    String timecardToken,
    boolean needsAvailability
) {}
