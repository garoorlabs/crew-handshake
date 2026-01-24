package com.crewhandshake.common.errors;

import java.util.Map;

public record ApiErrorResponse(
    String errorCode,
    String message,
    Map<String, String> fieldErrors
) {}
