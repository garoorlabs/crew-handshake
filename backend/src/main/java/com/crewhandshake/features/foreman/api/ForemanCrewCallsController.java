package com.crewhandshake.features.foreman.api;

import com.crewhandshake.features.foreman.service.CrewCallService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/foreman")
public class ForemanCrewCallsController {
  private final CrewCallService crewCallService;

  public ForemanCrewCallsController(CrewCallService crewCallService) {
    this.crewCallService = crewCallService;
  }

  @GetMapping("/crews")
  public List<ForemanCrewSummary> listCrews() {
    return crewCallService.getForemanCrews();
  }

  @GetMapping("/sites")
  public List<SiteSummary> listSites() {
    return crewCallService.getCompanySites();
  }

  @GetMapping("/crew-calls")
  public List<CrewCallSummaryResponse> listCrewCalls(@RequestParam String date, @RequestParam UUID crewId) {
    return crewCallService.getCrewCalls(date, crewId);
  }

  @PostMapping("/crew-calls")
  public CrewCallResponse createCrewCall(@Valid @RequestBody CrewCallCreateRequest request) {
    return crewCallService.createCrewCall(request);
  }

  @PostMapping("/crew-calls/{id}/resend")
  public CrewCallResponse resendCrewCall(@PathVariable("id") UUID crewCallId, @Valid @RequestBody CrewCallUpdateRequest request) {
    return crewCallService.resendCrewCall(crewCallId, request);
  }
}
