package com.crewhandshake.features.admin.api;

import com.crewhandshake.common.tenant.DispatchAuthority;
import com.crewhandshake.common.tenant.PayrollFrequency;
import java.time.DayOfWeek;

public record SettingsResponse(
    String defaultLanguage,
    PayrollFrequency payrollFrequency,
    DayOfWeek payrollCutoffDay,
    String standbyCutoffTime,
    DispatchAuthority dispatchAuthority
) {}
