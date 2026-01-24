package com.crewhandshake.features.worker.api;

import java.time.LocalDate;
import java.util.List;

public record WorkerTimecardResponse(
    LocalDate weekStart,
    LocalDate weekEnd,
    List<WorkerTimecardEntry> entries
) {}
