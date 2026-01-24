package com.crewhandshake.features.payroll.api;

import com.crewhandshake.features.payroll.service.PayrollService;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/payroll")
public class AdminPayrollController {
  private final PayrollService payrollService;

  public AdminPayrollController(PayrollService payrollService) {
    this.payrollService = payrollService;
  }

  @GetMapping("/periods/current")
  public PayrollSummaryResponse currentPeriod() {
    return payrollService.getCurrentPeriodSummary();
  }

  @GetMapping("/periods/{id}")
  public PayrollPeriodResponse period(@PathVariable("id") String periodId) {
    return payrollService.getPeriod(periodId);
  }

  @GetMapping("/periods/{id}/export")
  public ResponseEntity<byte[]> export(@PathVariable("id") String periodId) throws IOException {
    byte[] csv = payrollService.exportPeriodCsv(periodId);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"payroll-" + periodId + ".csv\"")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(csv);
  }
}
