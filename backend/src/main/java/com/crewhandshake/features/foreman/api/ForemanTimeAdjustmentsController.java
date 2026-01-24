package com.crewhandshake.features.foreman.api;

import com.crewhandshake.features.foreman.service.TimeAdjustmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/foreman/time-adjustments")
public class ForemanTimeAdjustmentsController {
  private final TimeAdjustmentService timeAdjustmentService;

  public ForemanTimeAdjustmentsController(TimeAdjustmentService timeAdjustmentService) {
    this.timeAdjustmentService = timeAdjustmentService;
  }

  @PostMapping
  public TimeAdjustmentResponse adjust(@Valid @RequestBody TimeAdjustmentRequest request) {
    return timeAdjustmentService.adjustTime(request);
  }
}
