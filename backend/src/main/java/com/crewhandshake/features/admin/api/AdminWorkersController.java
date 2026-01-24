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
@RequestMapping("/api/v1/admin/workers")
public class AdminWorkersController {
  private final AdminService adminService;

  public AdminWorkersController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping
  public List<WorkerResponse> listWorkers() {
    return adminService.getWorkers();
  }

  @PostMapping
  public WorkerResponse createWorker(@Valid @RequestBody WorkerCreateRequest request) {
    return adminService.createWorker(request);
  }

  @PutMapping
  public WorkerResponse updateWorker(@Valid @RequestBody WorkerUpdateRequest request) {
    return adminService.updateWorker(request);
  }
}
