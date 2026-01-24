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
@RequestMapping("/api/v1/admin/sites")
public class AdminSitesController {
  private final AdminService adminService;

  public AdminSitesController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping
  public List<SiteResponse> listSites() {
    return adminService.getSites();
  }

  @PostMapping
  public SiteResponse createSite(@Valid @RequestBody SiteCreateRequest request) {
    return adminService.createSite(request);
  }

  @PutMapping
  public SiteResponse updateSite(@Valid @RequestBody SiteUpdateRequest request) {
    return adminService.updateSite(request);
  }
}
