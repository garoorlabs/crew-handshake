package com.crewhandshake.features.foreman.api;

import com.crewhandshake.features.foreman.service.RollCallService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/foreman/roll-call")
public class ForemanRollCallController {
  private final RollCallService rollCallService;

  public ForemanRollCallController(RollCallService rollCallService) {
    this.rollCallService = rollCallService;
  }

  @PostMapping
  public RollCallResponse submit(@Valid @RequestBody RollCallRequest request) {
    return rollCallService.submitRollCall(request);
  }
}
