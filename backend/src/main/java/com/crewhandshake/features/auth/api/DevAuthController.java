package com.crewhandshake.features.auth.api;

import com.crewhandshake.features.auth.service.DevAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/dev")
public class DevAuthController {
  private final DevAuthService devAuthService;

  public DevAuthController(DevAuthService devAuthService) {
    this.devAuthService = devAuthService;
  }

  @PostMapping("/login")
  public MeResponse login(@Valid @RequestBody DevLoginRequest request) {
    return devAuthService.login(request.phone());
  }

  @PostMapping("/worker-link")
  public DevWorkerLinkResponse createWorkerLink() {
    return devAuthService.createWorkerLink();
  }
}
