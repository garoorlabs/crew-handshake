package com.crewhandshake.features.auth.api;

import java.util.UUID;

public record DevWorkerLinkResponse(
    String url,
    String token,
    UUID crewCallId,
    String phoneE164
) {}
