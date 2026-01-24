package com.crewhandshake.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.otp")
public record OtpProperties(
    Integer phoneRateLimitMax,
    Duration phoneRateLimitWindow,
    Integer ipRateLimitMax,
    Duration ipRateLimitWindow,
    Duration attemptRetention
) {}
