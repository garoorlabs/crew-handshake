package com.crewhandshake.features.foreman.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.tenant.TenantContext;
import com.crewhandshake.features.admin.api.AuditLogResponse;
import com.crewhandshake.features.admin.persistence.ForemanProfileRepository;
import com.crewhandshake.features.admin.persistence.WorkerProfileRepository;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import com.crewhandshake.features.auth.persistence.MembershipRepository;
import com.crewhandshake.features.foreman.persistence.AuditLogEntity;
import com.crewhandshake.features.foreman.persistence.AuditLogRepository;
import com.crewhandshake.features.foreman.persistence.TimeEntryEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
  private final TenantContext tenantContext;
  private final AuditLogRepository auditLogRepository;
  private final MembershipRepository membershipRepository;
  private final ForemanProfileRepository foremanProfileRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final ObjectMapper objectMapper;

  public AuditLogService(TenantContext tenantContext,
                         AuditLogRepository auditLogRepository,
                         MembershipRepository membershipRepository,
                         ForemanProfileRepository foremanProfileRepository,
                         WorkerProfileRepository workerProfileRepository,
                         ObjectMapper objectMapper) {
    this.tenantContext = tenantContext;
    this.auditLogRepository = auditLogRepository;
    this.membershipRepository = membershipRepository;
    this.foremanProfileRepository = foremanProfileRepository;
    this.workerProfileRepository = workerProfileRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public void logTimeAdjustment(MembershipEntity actor, TimeEntryEntity timeEntry, String reason, String note) {
    String details = toJson(Map.of(
        "reason", reason,
        "note", note,
        "checkInAt", timeEntry.getCheckInAt(),
        "checkOutAt", timeEntry.getCheckOutAt()
    ));
    AuditLogEntity log = new AuditLogEntity(
        timeEntry.getCompany(),
        actor,
        "TIME_ADJUSTMENT",
        "TIME_ENTRY",
        timeEntry.getId(),
        details,
        Instant.now()
    );
    auditLogRepository.save(log);
  }

  @Transactional(readOnly = true)
  public List<AuditLogResponse> getAuditLogs() {
    tenantContext.requireAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    return auditLogRepository.findByCompanyId(companyId).stream()
        .map(log -> new AuditLogResponse(
            log.getId(),
            log.getActionType(),
            log.getEntityType(),
            log.getEntityId(),
            resolveActorName(log.getActorMembership().getId(), companyId),
            log.getCreatedAt(),
            log.getDetailsJson()
        ))
        .collect(Collectors.toList());
  }

  private String resolveActorName(UUID membershipId, UUID companyId) {
    return foremanProfileRepository.findByCompanyIdAndMembershipId(companyId, membershipId)
        .map(profile -> profile.getDisplayName())
        .or(() -> workerProfileRepository.findByCompanyIdAndMembershipId(companyId, membershipId)
            .map(profile -> profile.getDisplayName()))
        .orElseGet(() -> membershipRepository.findByCompanyIdAndId(companyId, membershipId)
            .map(membership -> membership.getIdentity().getPhoneE164())
            .orElse("User"));
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException ex) {
      throw new ApiException(ApiErrorCode.UNKNOWN, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to write audit log");
    }
  }
}
