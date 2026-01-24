package com.crewhandshake.features.foreman.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.tenant.TenantContext;
import com.crewhandshake.common.time.TimeParser;
import com.crewhandshake.features.admin.persistence.CrewEntity;
import com.crewhandshake.features.admin.persistence.CrewRepository;
import com.crewhandshake.features.admin.persistence.WorkerProfileEntity;
import com.crewhandshake.features.admin.persistence.WorkerProfileRepository;
import com.crewhandshake.features.foreman.api.RollCallEntry;
import com.crewhandshake.features.foreman.api.RollCallRequest;
import com.crewhandshake.features.foreman.api.RollCallResponse;
import com.crewhandshake.features.foreman.api.RollCallStatus;
import com.crewhandshake.features.foreman.persistence.CrewCallEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRepository;
import com.crewhandshake.features.foreman.persistence.TimeEntryEntity;
import com.crewhandshake.features.foreman.persistence.TimeEntryRepository;
import com.crewhandshake.features.foreman.persistence.TimeEntrySource;
import com.crewhandshake.features.foreman.persistence.TimeEntryStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RollCallService {
  private final TenantContext tenantContext;
  private final TimeParser timeParser;
  private final CrewRepository crewRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final CrewCallRepository crewCallRepository;
  private final TimeEntryRepository timeEntryRepository;

  public RollCallService(TenantContext tenantContext,
                         TimeParser timeParser,
                         CrewRepository crewRepository,
                         WorkerProfileRepository workerProfileRepository,
                         CrewCallRepository crewCallRepository,
                         TimeEntryRepository timeEntryRepository) {
    this.tenantContext = tenantContext;
    this.timeParser = timeParser;
    this.crewRepository = crewRepository;
    this.workerProfileRepository = workerProfileRepository;
    this.crewCallRepository = crewCallRepository;
    this.timeEntryRepository = timeEntryRepository;
  }

  @Transactional
  public RollCallResponse submitRollCall(RollCallRequest request) {
    tenantContext.requireForemanOrAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    CrewEntity crew = crewRepository.findByCompanyIdAndId(companyId, request.crewId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Crew not found"));
    ensureCrewScope(crew);
    LocalDate workDate = timeParser.parseDate(request.date(), "date");
    Instant recordedAt = request.recordedAt() == null ? Instant.now() : timeParser.parseInstant(request.recordedAt(), "recordedAt");

    CrewCallEntity crewCall = crewCallRepository.findTopByCompanyIdAndCrewIdAndWorkDateOrderByCreatedAtDesc(companyId, crew.getId(), workDate)
        .orElseThrow(() -> new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Crew call required"));

    Map<UUID, WorkerProfileEntity> roster = workerProfileRepository.findByCompanyIdAndCrewId(companyId, crew.getId()).stream()
        .collect(Collectors.toMap(WorkerProfileEntity::getMembershipId, worker -> worker));

    int updated = 0;
    for (RollCallEntry entry : request.entries()) {
      WorkerProfileEntity worker = roster.get(entry.workerMembershipId());
      if (worker == null) {
        throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Worker not on roster",
            java.util.Map.of("workerMembershipId", "Worker is not on this crew"));
      }
      if (entry.status() == RollCallStatus.ABSENT) {
        continue;
      }
      TimeEntryEntity timeEntry = timeEntryRepository
          .findByCompanyIdAndWorkerMembershipIdAndCrewCallIdAndWorkDate(companyId, worker.getMembershipId(), crewCall.getId(), workDate)
          .orElseGet(() -> new TimeEntryEntity(crewCall.getCompany(), worker.getMembership(), crewCall, workDate,
              TimeEntrySource.FOREMAN_ROLL_CALL, TimeEntryStatus.PENDING, Instant.now()));
      if (timeEntry.getCheckInAt() == null) {
        timeEntry.setCheckInAt(recordedAt);
        timeEntry.setSource(TimeEntrySource.FOREMAN_ROLL_CALL);
        updated++;
      }
      timeEntryRepository.save(timeEntry);
    }

    return new RollCallResponse(crew.getId(), workDate.toString(), updated);
  }

  private void ensureCrewScope(CrewEntity crew) {
    if (tenantContext.isAdmin()) {
      return;
    }
    UUID membershipId = tenantContext.requireMembershipId();
    if (!crew.getForemanMembership().getId().equals(membershipId)) {
      throw new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Not permitted");
    }
  }
}
