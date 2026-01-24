package com.crewhandshake.features.foreman.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.tenant.TenantContext;
import com.crewhandshake.common.time.TimeParser;
import com.crewhandshake.features.admin.persistence.CrewEntity;
import com.crewhandshake.features.admin.persistence.CrewRepository;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import com.crewhandshake.features.auth.persistence.MembershipRepository;
import com.crewhandshake.features.foreman.api.TimeAdjustmentRequest;
import com.crewhandshake.features.foreman.api.TimeAdjustmentResponse;
import com.crewhandshake.features.foreman.persistence.TimeEntryEntity;
import com.crewhandshake.features.foreman.persistence.TimeEntryRepository;
import com.crewhandshake.features.foreman.persistence.TimeEntryStatus;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TimeAdjustmentService {
  private final TenantContext tenantContext;
  private final TimeParser timeParser;
  private final TimeEntryRepository timeEntryRepository;
  private final CrewRepository crewRepository;
  private final MembershipRepository membershipRepository;
  private final AuditLogService auditLogService;

  public TimeAdjustmentService(TenantContext tenantContext,
                               TimeParser timeParser,
                               TimeEntryRepository timeEntryRepository,
                               CrewRepository crewRepository,
                               MembershipRepository membershipRepository,
                               AuditLogService auditLogService) {
    this.tenantContext = tenantContext;
    this.timeParser = timeParser;
    this.timeEntryRepository = timeEntryRepository;
    this.crewRepository = crewRepository;
    this.membershipRepository = membershipRepository;
    this.auditLogService = auditLogService;
  }

  @Transactional
  public TimeAdjustmentResponse adjustTime(TimeAdjustmentRequest request) {
    tenantContext.requireForemanOrAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    TimeEntryEntity timeEntry = timeEntryRepository.findById(request.timeEntryId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Time entry not found"));
    if (!timeEntry.getCompany().getId().equals(companyId)) {
      throw new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Not permitted");
    }
    ensureCrewScope(timeEntry);

    Instant checkInAt = timeParser.parseInstant(request.checkInAt(), "checkInAt");
    Instant checkOutAt = timeParser.parseInstant(request.checkOutAt(), "checkOutAt");
    if (checkOutAt.isBefore(checkInAt)) {
      throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Invalid time range",
          Map.of("checkOutAt", "Check-out must be after check-in"));
    }

    timeEntry.setCheckInAt(checkInAt);
    timeEntry.setCheckOutAt(checkOutAt);
    timeEntry.markEdited(request.reason().trim(), request.note());
    timeEntry.setStatus(tenantContext.isAdmin() ? TimeEntryStatus.APPROVED : TimeEntryStatus.PENDING);
    timeEntryRepository.save(timeEntry);

    MembershipEntity actor = membershipRepository.findByCompanyIdAndId(companyId, tenantContext.requireMembershipId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Membership not found"));

    auditLogService.logTimeAdjustment(actor, timeEntry, request.reason(), request.note());

    return new TimeAdjustmentResponse(timeEntry.getId(), timeEntry.getCheckInAt(), timeEntry.getCheckOutAt(), timeEntry.getStatus(), timeEntry.isEdited());
  }

  private void ensureCrewScope(TimeEntryEntity timeEntry) {
    if (tenantContext.isAdmin()) {
      return;
    }
    CrewEntity crew = crewRepository.findByCompanyIdAndId(timeEntry.getCompany().getId(), timeEntry.getCrewCall().getCrew().getId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Crew not found"));
    UUID membershipId = tenantContext.requireMembershipId();
    if (!crew.getForemanMembership().getId().equals(membershipId)) {
      throw new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Not permitted");
    }
  }
}
