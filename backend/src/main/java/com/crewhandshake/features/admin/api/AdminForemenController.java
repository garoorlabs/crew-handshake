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
@RequestMapping("/api/v1/admin/foremen")
public class AdminForemenController {
  private final AdminService adminService;

  public AdminForemenController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping
  public List<ForemanResponse> listForemen() {
    return adminService.getForemen();
  }

  @PostMapping
  public ForemanResponse createForeman(@Valid @RequestBody ForemanCreateRequest request) {
    return adminService.createForeman(request);
  }

  @PutMapping
  public ForemanResponse updateForeman(@Valid @RequestBody ForemanUpdateRequest request) {
    return adminService.updateForeman(request);
  }
}
