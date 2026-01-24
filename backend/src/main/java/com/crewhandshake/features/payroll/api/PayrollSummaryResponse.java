package com.crewhandshake.features.payroll.api;

import java.time.LocalDate;

public record PayrollSummaryResponse(
    String periodId,
    LocalDate periodStart,
    LocalDate periodEnd,
    int totalEntries,
    int unresolvedExceptions
) {}
