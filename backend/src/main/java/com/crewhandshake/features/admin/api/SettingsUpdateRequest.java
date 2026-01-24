package com.crewhandshake.features.admin.api;

import com.crewhandshake.common.tenant.DispatchAuthority;
import com.crewhandshake.common.tenant.PayrollFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;

public record SettingsUpdateRequest(
    @NotBlank(message = "Default language is required")
    String defaultLanguage,
    @NotNull(message = "Payroll frequency is required")
    PayrollFrequency payrollFrequency,
    @NotNull(message = "Payroll cutoff day is required")
    DayOfWeek payrollCutoffDay,
    @NotBlank(message = "Standby cutoff time is required")
    String standbyCutoffTime,
    @NotNull(message = "Dispatch authority is required")
    DispatchAuthority dispatchAuthority
) {}
