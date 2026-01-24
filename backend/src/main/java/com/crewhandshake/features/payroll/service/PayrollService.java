package com.crewhandshake.features.payroll.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.tenant.PayrollFrequency;
import com.crewhandshake.common.tenant.TenantContext;
import com.crewhandshake.features.admin.persistence.WorkerProfileRepository;
import com.crewhandshake.features.auth.persistence.CompanyEntity;
import com.crewhandshake.features.auth.persistence.CompanyRepository;
import com.crewhandshake.features.foreman.persistence.ExceptionRepository;
import com.crewhandshake.features.foreman.persistence.ExceptionStatus;
import com.crewhandshake.features.foreman.persistence.TimeEntryEntity;
import com.crewhandshake.features.foreman.persistence.TimeEntryRepository;
import com.crewhandshake.features.payroll.api.PayrollEntryResponse;
import com.crewhandshake.features.payroll.api.PayrollPeriodResponse;
import com.crewhandshake.features.payroll.api.PayrollSummaryResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayrollService {
  private final TenantContext tenantContext;
  private final CompanyRepository companyRepository;
  private final TimeEntryRepository timeEntryRepository;
  private final ExceptionRepository exceptionRepository;
  private final WorkerProfileRepository workerProfileRepository;

  public PayrollService(TenantContext tenantContext,
                        CompanyRepository companyRepository,
                        TimeEntryRepository timeEntryRepository,
                        ExceptionRepository exceptionRepository,
                        WorkerProfileRepository workerProfileRepository) {
    this.tenantContext = tenantContext;
    this.companyRepository = companyRepository;
    this.timeEntryRepository = timeEntryRepository;
    this.exceptionRepository = exceptionRepository;
    this.workerProfileRepository = workerProfileRepository;
  }

  @Transactional(readOnly = true)
  public PayrollSummaryResponse getCurrentPeriodSummary() {
    CompanyEntity company = requireCompany();
    PayrollPeriod period = calculatePeriod(company, LocalDate.now(ZoneOffset.UTC));
    int totalEntries = (int) timeEntryRepository.countByCompanyIdAndWorkDateBetween(company.getId(), period.start, period.end);
    int unresolved = (int) exceptionRepository.countByCompanyIdAndStatus(company.getId(), ExceptionStatus.OPEN);
    return new PayrollSummaryResponse(period.id, period.start, period.end, totalEntries, unresolved);
  }

  @Transactional(readOnly = true)
  public PayrollPeriodResponse getPeriod(String periodId) {
    CompanyEntity company = requireCompany();
    PayrollPeriod period = parsePeriod(periodId);
    List<TimeEntryEntity> entries = timeEntryRepository.findByCompanyIdAndWorkDateBetween(company.getId(), period.start, period.end);
    Map<UUID, String> workerNames = workerProfileRepository.findByCompanyId(company.getId()).stream()
        .collect(Collectors.toMap(worker -> worker.getMembershipId(), worker -> worker.getDisplayName()));
    List<PayrollEntryResponse> responseEntries = entries.stream()
        .map(entry -> new PayrollEntryResponse(
            entry.getId(),
            entry.getWorkerMembership().getId(),
            workerNames.getOrDefault(entry.getWorkerMembership().getId(), entry.getWorkerMembership().getIdentity().getPhoneE164()),
            entry.getWorkDate(),
            entry.getCheckInAt(),
            entry.getCheckOutAt(),
            entry.getStatus(),
            entry.isEdited()
        ))
        .collect(Collectors.toList());
    int unresolved = (int) exceptionRepository.countByCompanyIdAndStatus(company.getId(), ExceptionStatus.OPEN);
    return new PayrollPeriodResponse(period.id, period.start, period.end, responseEntries.size(), unresolved, responseEntries);
  }

  @Transactional(readOnly = true)
  public byte[] exportPeriodCsv(String periodId) throws IOException {
    PayrollPeriodResponse period = getPeriod(periodId);
    StringBuilder sb = new StringBuilder();
    sb.append("workerName,workDate,checkInAt,checkOutAt,status,edited\n");
    for (PayrollEntryResponse entry : period.entries()) {
      sb.append(escape(entry.workerName())).append(",");
      sb.append(entry.workDate()).append(",");
      sb.append(entry.checkInAt() == null ? "" : entry.checkInAt()).append(",");
      sb.append(entry.checkOutAt() == null ? "" : entry.checkOutAt()).append(",");
      sb.append(entry.status()).append(",");
      sb.append(entry.edited()).append("\n");
    }
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  private CompanyEntity requireCompany() {
    tenantContext.requireAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Company not found"));
  }

  private PayrollPeriod calculatePeriod(CompanyEntity company, LocalDate today) {
    DayOfWeek cutoffDay = company.getPayrollCutoffDay();
    LocalDate periodEnd = today;
    while (periodEnd.getDayOfWeek() != cutoffDay) {
      periodEnd = periodEnd.minusDays(1);
    }
    int length = company.getPayrollFrequency() == PayrollFrequency.BIWEEKLY ? 14 : 7;
    LocalDate periodStart = periodEnd.minusDays(length - 1);
    return new PayrollPeriod(periodStart, periodEnd);
  }

  private PayrollPeriod parsePeriod(String periodId) {
    String[] parts = periodId.split("_");
    if (parts.length != 2) {
      throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Invalid period id");
    }
    try {
      LocalDate start = LocalDate.parse(parts[0]);
      LocalDate end = LocalDate.parse(parts[1]);
      return new PayrollPeriod(start, end);
    } catch (Exception ex) {
      throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Invalid period id");
    }
  }

  private String escape(String value) {
    if (value == null) {
      return "";
    }
    String escaped = value.replace("\"", "\"\"");
    if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
      return "\"" + escaped + "\"";
    }
    return escaped;
  }

  private static class PayrollPeriod {
    private final LocalDate start;
    private final LocalDate end;
    private final String id;

    private PayrollPeriod(LocalDate start, LocalDate end) {
      this.start = start;
      this.end = end;
      this.id = start + "_" + end;
    }
  }
}
