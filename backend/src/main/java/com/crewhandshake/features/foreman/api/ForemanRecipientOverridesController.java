package com.crewhandshake.features.foreman.api;

import com.crewhandshake.features.foreman.service.RecipientOverrideService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/foreman/recipient-overrides")
public class ForemanRecipientOverridesController {
  private final RecipientOverrideService recipientOverrideService;

  public ForemanRecipientOverridesController(RecipientOverrideService recipientOverrideService) {
    this.recipientOverrideService = recipientOverrideService;
  }

  @PostMapping
  public CrewCallRecipientStatus sendOverride(@Valid @RequestBody RecipientOverrideRequest request) {
    return recipientOverrideService.applyOverride(request);
  }
}
