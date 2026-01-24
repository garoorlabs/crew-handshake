package com.crewhandshake.features.worker.api;

import com.crewhandshake.features.worker.service.WorkerLinkService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/worker")
public class WorkerPublicController {
  private final WorkerLinkService workerLinkService;

  public WorkerPublicController(WorkerLinkService workerLinkService) {
    this.workerLinkService = workerLinkService;
  }

  @GetMapping("/links/{token}")
  public WorkerLinkResolutionResponse resolveLink(@PathVariable("token") String token) {
    return workerLinkService.resolveLink(token);
  }

  @GetMapping("/crew-calls/by-link/{token}")
  public WorkerCrewCallResponse getCrewCall(@PathVariable("token") String token) {
    return workerLinkService.getCrewCall(token);
  }

  @PostMapping("/crew-calls/by-link/{token}/handshake")
  public WorkerCrewCallResponse submitHandshake(@PathVariable("token") String token,
                                                @Valid @RequestBody WorkerHandshakeRequest request) {
    return workerLinkService.submitHandshake(token, request);
  }

  @PostMapping("/crew-calls/by-link/{token}/availability")
  public WorkerCrewCallResponse submitAvailability(@PathVariable("token") String token,
                                                   @Valid @RequestBody WorkerAvailabilityRequest request) {
    return workerLinkService.submitAvailability(token, request);
  }

  @PostMapping("/crew-calls/by-link/{token}/check-in")
  public WorkerCheckInResponse checkIn(@PathVariable("token") String token) {
    return workerLinkService.checkIn(token);
  }

  @PostMapping("/crew-calls/by-link/{token}/check-out")
  public WorkerCheckOutResponse checkOut(@PathVariable("token") String token) {
    return workerLinkService.checkOut(token);
  }

  @GetMapping("/timecard/by-link/{token}")
  public WorkerTimecardResponse getTimecard(@PathVariable("token") String token,
                                            @RequestParam(required = false) String week) {
    return workerLinkService.getTimecard(token, week);
  }

  @PostMapping("/timecard/by-link/{token}/review-requests")
  public WorkerReviewRequestResponse submitReviewRequest(@PathVariable("token") String token,
                                                         @Valid @RequestBody WorkerReviewRequestCreateRequest request) {
    return workerLinkService.submitReviewRequest(token, request);
  }
}
