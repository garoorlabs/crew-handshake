package com.crewhandshake.features.foreman.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.tenant.TenantContext;
import com.crewhandshake.common.time.TimeParser;
import com.crewhandshake.features.admin.persistence.CrewEntity;
import com.crewhandshake.features.admin.persistence.CrewRepository;
import com.crewhandshake.features.admin.persistence.WorkerProfileEntity;
import com.crewhandshake.features.admin.persistence.WorkerProfileRepository;
import com.crewhandshake.features.foreman.api.TodayBoardResponse;
import com.crewhandshake.features.foreman.api.TodayWorkerStatus;
import com.crewhandshake.features.foreman.persistence.CrewCallEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientRepository;
import com.crewhandshake.features.foreman.persistence.CrewCallRepository;
import com.crewhandshake.features.foreman.persistence.ExceptionEntity;
import com.crewhandshake.features.foreman.persistence.ExceptionRepository;
import com.crewhandshake.features.foreman.persistence.ExceptionStatus;
import com.crewhandshake.features.foreman.persistence.TimeEntryEntity;
import com.crewhandshake.features.foreman.persistence.TimeEntryRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodayBoardService {
  private final TenantContext tenantContext;
  private final TimeParser timeParser;
  private final CrewRepository crewRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final CrewCallRepository crewCallRepository;
  private final CrewCallRecipientRepository crewCallRecipientRepository;
  private final TimeEntryRepository timeEntryRepository;
  private final ExceptionRepository exceptionRepository;

  public TodayBoardService(TenantContext tenantContext,
                           TimeParser timeParser,
                           CrewRepository crewRepository,
                           WorkerProfileRepository workerProfileRepository,
                           CrewCallRepository crewCallRepository,
                           CrewCallRecipientRepository crewCallRecipientRepository,
                           TimeEntryRepository timeEntryRepository,
                           ExceptionRepository exceptionRepository) {
    this.tenantContext = tenantContext;
    this.timeParser = timeParser;
    this.crewRepository = crewRepository;
    this.workerProfileRepository = workerProfileRepository;
    this.crewCallRepository = crewCallRepository;
    this.crewCallRecipientRepository = crewCallRecipientRepository;
    this.timeEntryRepository = timeEntryRepository;
    this.exceptionRepository = exceptionRepository;
  }

  @Transactional(readOnly = true)
  public TodayBoardResponse getTodayBoard(String date, UUID crewId) {
    tenantContext.requireForemanOrAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    CrewEntity crew = crewRepository.findByCompanyIdAndId(companyId, crewId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Crew not found"));
    ensureCrewScope(crew);
    LocalDate workDate = timeParser.parseDate(date, "date");

    CrewCallEntity crewCall = crewCallRepository.findTopByCompanyIdAndCrewIdAndWorkDateOrderByCreatedAtDesc(companyId, crewId, workDate)
        .orElse(null);

    Map<UUID, CrewCallRecipientEntity> recipientByWorker = crewCall == null
        ? Map.of()
        : crewCallRecipientRepository.findByCompanyIdAndCrewCallId(companyId, crewCall.getId()).stream()
            .collect(Collectors.toMap(recipient -> recipient.getWorkerMembership().getId(), recipient -> recipient));

    Map<UUID, TimeEntryEntity> timeEntryByWorker = crewCall == null
        ? Map.of()
        : timeEntryRepository.findByCompanyIdAndWorkDateAndCrewCallId(companyId, workDate, crewCall.getId()).stream()
            .collect(Collectors.toMap(entry -> entry.getWorkerMembership().getId(), entry -> entry));

    Map<UUID, Boolean> exceptionByWorker = exceptionRepository.findByCompanyIdAndCrewIdAndStatus(companyId, crewId, ExceptionStatus.OPEN)
        .stream()
        .collect(Collectors.toMap(exception -> exception.getWorkerMembership().getId(), exception -> true, (a, b) -> true));

    List<TodayWorkerStatus> workers = workerProfileRepository.findByCompanyIdAndCrewId(companyId, crewId).stream()
        .map(worker -> {
          CrewCallRecipientEntity recipient = recipientByWorker.get(worker.getMembershipId());
          TimeEntryEntity timeEntry = timeEntryByWorker.get(worker.getMembershipId());
          boolean hasException = exceptionByWorker.getOrDefault(worker.getMembershipId(), false);
          if (timeEntry != null && timeEntry.getCheckInAt() != null && timeEntry.getCheckOutAt() == null) {
            hasException = true;
          }
          return new TodayWorkerStatus(
              worker.getMembershipId(),
              worker.getDisplayName(),
              worker.getMembership().getIdentity().getPhoneE164(),
              recipient == null ? null : recipient.getHandshakeStatus(),
              recipient == null ? null : recipient.getLateEtaMinutes(),
              timeEntry == null ? null : timeEntry.getCheckInAt(),
              timeEntry == null ? null : timeEntry.getCheckOutAt(),
              hasException,
              timeEntry == null ? null : timeEntry.getId()
          );
        })
        .collect(Collectors.toList());

    return new TodayBoardResponse(
        crew.getId(),
        crew.getName(),
        workDate.toString(),
        crewCall == null ? null : crewCall.getId(),
        crewCall == null ? null : crewCall.getSite().getName(),
        crewCall == null ? null : crewCall.getStartAt(),
        crewCall == null ? null : crewCall.getMeetPoint(),
        workers
    );
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
