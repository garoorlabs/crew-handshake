package com.crewhandshake.features.admin.api;

import com.crewhandshake.features.foreman.service.AuditLogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit")
public class AdminAuditController {
  private final AuditLogService auditLogService;

  public AdminAuditController(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @GetMapping
  public List<AuditLogResponse> listAuditLogs() {
    return auditLogService.getAuditLogs();
  }
}
