package com.crewhandshake.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    String publicBaseUrl,
    String smsProvider,
    List<String> trustedProxies
) {}
