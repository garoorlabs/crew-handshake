package com.crewhandshake.features.payroll.api;

import java.time.LocalDate;
import java.util.List;

public record PayrollPeriodResponse(
    String periodId,
    LocalDate periodStart,
    LocalDate periodEnd,
    int totalEntries,
    int unresolvedExceptions,
    List<PayrollEntryResponse> entries
) {}
