package com.crewhandshake.features.admin.api;

import com.crewhandshake.features.admin.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/settings")
public class AdminSettingsController {
  private final AdminService adminService;

  public AdminSettingsController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping
  public SettingsResponse getSettings() {
    return adminService.getSettings();
  }

  @PutMapping
  public SettingsResponse updateSettings(@Valid @RequestBody SettingsUpdateRequest request) {
    return adminService.updateSettings(request);
  }
}
