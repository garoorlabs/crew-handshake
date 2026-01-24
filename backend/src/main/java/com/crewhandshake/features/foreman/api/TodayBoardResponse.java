package com.crewhandshake.features.foreman.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TodayBoardResponse(
    UUID crewId,
    String crewName,
    String date,
    UUID crewCallId,
    String siteName,
    Instant startAt,
    String meetPoint,
    List<TodayWorkerStatus> workers
) {}
