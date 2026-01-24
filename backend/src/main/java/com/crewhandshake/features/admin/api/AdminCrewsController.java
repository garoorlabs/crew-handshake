package com.crewhandshake.features.admin.api;

import com.crewhandshake.features.admin.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/crews")
public class AdminCrewsController {
  private final AdminService adminService;

  public AdminCrewsController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping
  public List<CrewResponse> listCrews() {
    return adminService.getCrews();
  }

  @PostMapping
  public CrewResponse createCrew(@Valid @RequestBody CrewCreateRequest request) {
    return adminService.createCrew(request);
  }

  @PutMapping
  public CrewResponse updateCrew(@Valid @RequestBody CrewUpdateRequest request) {
    return adminService.updateCrew(request);
  }
}
