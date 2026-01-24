package com.crewhandshake.features.auth.api;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.features.auth.service.MeService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MeController {
  private final MeService meService;

  public MeController(MeService meService) {
    this.meService = meService;
  }

  @GetMapping("/me")
  public MeResponse getMe() {
    return meService.getMe();
  }

  @PostMapping("/me/active-company")
  public MeResponse setActiveCompany(@Valid @RequestBody ActiveCompanyRequest request) {
    UUID companyId;
    try {
      companyId = UUID.fromString(request.companyId());
    } catch (IllegalArgumentException ex) {
      throw new ApiException(
          ApiErrorCode.VALIDATION_ERROR,
          HttpStatus.BAD_REQUEST,
          "Invalid company ID",
          java.util.Map.of("companyId", "Invalid company ID")
      );
    }
    return meService.setActiveCompany(companyId);
  }
}
