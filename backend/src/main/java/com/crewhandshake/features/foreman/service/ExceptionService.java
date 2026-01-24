package com.crewhandshake.features.foreman.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.tenant.TenantContext;
import com.crewhandshake.common.time.TimeParser;
import com.crewhandshake.features.admin.persistence.CrewEntity;
import com.crewhandshake.features.admin.persistence.CrewRepository;
import com.crewhandshake.features.admin.persistence.WorkerProfileRepository;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import com.crewhandshake.features.auth.persistence.MembershipRepository;
import com.crewhandshake.features.foreman.api.ExceptionResolveRequest;
import com.crewhandshake.features.foreman.api.ExceptionResponse;
import com.crewhandshake.features.foreman.persistence.CrewCallEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRepository;
import com.crewhandshake.features.foreman.persistence.ExceptionEntity;
import com.crewhandshake.features.foreman.persistence.ExceptionRepository;
import com.crewhandshake.features.foreman.persistence.ExceptionResolutionAction;
import com.crewhandshake.features.foreman.persistence.ExceptionStatus;
import com.crewhandshake.features.foreman.persistence.ExceptionType;
import com.crewhandshake.features.foreman.persistence.ReviewRequestEntity;
import com.crewhandshake.features.foreman.persistence.ReviewRequestRepository;
import com.crewhandshake.features.foreman.persistence.ReviewRequestStatus;
import com.crewhandshake.features.foreman.persistence.TimeEntryEntity;
import com.crewhandshake.features.foreman.persistence.TimeEntryRepository;
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
public class ExceptionService {
  private final TenantContext tenantContext;
  private final TimeParser timeParser;
  private final CrewRepository crewRepository;
  private final CrewCallRepository crewCallRepository;
  private final TimeEntryRepository timeEntryRepository;
  private final ReviewRequestRepository reviewRequestRepository;
  private final ExceptionRepository exceptionRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final MembershipRepository membershipRepository;
  private final AuditLogService auditLogService;

  public ExceptionService(TenantContext tenantContext,
                          TimeParser timeParser,
                          CrewRepository crewRepository,
                          CrewCallRepository crewCallRepository,
                          TimeEntryRepository timeEntryRepository,
                          ReviewRequestRepository reviewRequestRepository,
                          ExceptionRepository exceptionRepository,
                          WorkerProfileRepository workerProfileRepository,
                          MembershipRepository membershipRepository,
                          AuditLogService auditLogService) {
    this.tenantContext = tenantContext;
    this.timeParser = timeParser;
    this.crewRepository = crewRepository;
    this.crewCallRepository = crewCallRepository;
    this.timeEntryRepository = timeEntryRepository;
    this.reviewRequestRepository = reviewRequestRepository;
    this.exceptionRepository = exceptionRepository;
    this.workerProfileRepository = workerProfileRepository;
    this.membershipRepository = membershipRepository;
    this.auditLogService = auditLogService;
  }

  @Transactional
  public List<ExceptionResponse> getExceptions(String date, UUID crewId) {
    tenantContext.requireForemanOrAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    CrewEntity crew = crewRepository.findByCompanyIdAndId(companyId, crewId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Crew not found"));
    ensureCrewScope(crew);
    LocalDate workDate = timeParser.parseDate(date, "date");

    CrewCallEntity crewCall = crewCallRepository.findTopByCompanyIdAndCrewIdAndWorkDateOrderByCreatedAtDesc(companyId, crewId, workDate)
        .orElse(null);
    if (crewCall != null) {
      List<TimeEntryEntity> timeEntries = timeEntryRepository.findByCompanyIdAndWorkDateAndCrewCallId(companyId, workDate, crewCall.getId());
      for (TimeEntryEntity timeEntry : timeEntries) {
        if (timeEntry.getCheckInAt() != null && timeEntry.getCheckOutAt() == null) {
          exceptionRepository.findByCompanyIdAndTimeEntryIdAndType(companyId, timeEntry.getId(), ExceptionType.MISSING_CHECK_OUT)
              .orElseGet(() -> exceptionRepository.save(new ExceptionEntity(
                  crewCall.getCompany(),
                  crew,
                  timeEntry.getWorkerMembership(),
                  ExceptionType.MISSING_CHECK_OUT,
                  ExceptionStatus.OPEN,
                  timeEntry,
                  null,
                  Instant.now()
              )));
        }
      }
    }

    List<ReviewRequestEntity> reviewRequests = reviewRequestRepository.findByCompanyIdAndWorkDateAndStatus(companyId, workDate, ReviewRequestStatus.OPEN);
    for (ReviewRequestEntity reviewRequest : reviewRequests) {
      if (workerProfileRepository.findByCompanyIdAndMembershipId(companyId, reviewRequest.getWorkerMembership().getId())
          .map(worker -> worker.getCrew() != null && worker.getCrew().getId().equals(crewId))
          .orElse(false)) {
        exceptionRepository.findByCompanyIdAndReviewRequestIdAndType(companyId, reviewRequest.getId(), ExceptionType.REVIEW_REQUEST)
            .orElseGet(() -> exceptionRepository.save(new ExceptionEntity(
                reviewRequest.getCompany(),
                crew,
                reviewRequest.getWorkerMembership(),
                ExceptionType.REVIEW_REQUEST,
                ExceptionStatus.OPEN,
                null,
                reviewRequest,
                Instant.now()
            )));
      }
    }

    return exceptionRepository.findByCompanyIdAndCrewIdAndStatus(companyId, crewId, ExceptionStatus.OPEN).stream()
        .map(exception -> {
          String workerName = workerProfileRepository.findByCompanyIdAndMembershipId(companyId, exception.getWorkerMembership().getId())
              .map(worker -> worker.getDisplayName())
              .orElse(exception.getWorkerMembership().getIdentity().getPhoneE164());
          TimeEntryEntity timeEntry = exception.getTimeEntry();
          ReviewRequestEntity reviewRequest = exception.getReviewRequest();
          return new ExceptionResponse(
              exception.getId(),
              exception.getType(),
              exception.getStatus(),
              crew.getId(),
              crew.getName(),
              exception.getWorkerMembership().getId(),
              workerName,
              timeEntry == null ? null : timeEntry.getId(),
              reviewRequest == null ? null : reviewRequest.getId(),
              timeEntry == null ? null : timeEntry.getCheckInAt(),
              timeEntry == null ? null : timeEntry.getCheckOutAt(),
              reviewRequest == null ? null : reviewRequest.getReason(),
              reviewRequest == null ? null : reviewRequest.getNote()
          );
        })
        .collect(Collectors.toList());
  }

  @Transactional
  public ExceptionResponse resolveException(UUID exceptionId, ExceptionResolveRequest request) {
    tenantContext.requireForemanOrAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    ExceptionEntity exception = exceptionRepository.findByCompanyIdAndId(companyId, exceptionId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Exception not found"));

    CrewEntity crew = exception.getCrew();
    ensureCrewScope(crew);
    if (request.action() == ExceptionResolutionAction.APPROVE_AS_IS && !tenantContext.isAdmin()) {
      throw new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Approval is restricted to admins");
    }

    TimeEntryEntity timeEntry = exception.getTimeEntry();
    ReviewRequestEntity reviewRequest = exception.getReviewRequest();
    if (request.action() == ExceptionResolutionAction.ADJUST_TIME) {
      if (request.reason() == null || request.reason().isBlank()) {
        throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Reason required",
            Map.of("reason", "Reason is required"));
      }
      Instant checkInAt = timeParser.parseInstant(request.checkInAt(), "checkInAt");
      Instant checkOutAt = timeParser.parseInstant(request.checkOutAt(), "checkOutAt");
      if (checkOutAt.isBefore(checkInAt)) {
        throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Invalid time range",
            Map.of("checkOutAt", "Check-out must be after check-in"));
      }
      if (timeEntry == null) {
        throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Time entry required");
      }
      timeEntry.setCheckInAt(checkInAt);
      timeEntry.setCheckOutAt(checkOutAt);
      timeEntry.markEdited(request.reason().trim(), request.note());
      timeEntry.setStatus(tenantContext.isAdmin() ? TimeEntryStatus.APPROVED : TimeEntryStatus.PENDING);
      timeEntryRepository.save(timeEntry);
      if (reviewRequest != null) {
        reviewRequest.setStatus(ReviewRequestStatus.RESOLVED);
        reviewRequestRepository.save(reviewRequest);
      }
      MembershipEntity actor = membershipRepository.findByCompanyIdAndId(companyId, tenantContext.requireMembershipId())
          .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Membership not found"));
      auditLogService.logTimeAdjustment(actor, timeEntry, request.reason(), request.note());
    } else if (request.action() == ExceptionResolutionAction.MARK_NO_SHOW) {
      if (request.reason() == null || request.reason().isBlank()) {
        throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Reason required",
            Map.of("reason", "Reason is required"));
      }
      if (timeEntry != null) {
        timeEntry.setCheckInAt(null);
        timeEntry.setCheckOutAt(null);
        timeEntry.markEdited(request.reason().trim(), request.note());
        timeEntry.setStatus(TimeEntryStatus.NEEDS_REVIEW);
        timeEntryRepository.save(timeEntry);
      }
      if (reviewRequest != null) {
        reviewRequest.setStatus(ReviewRequestStatus.RESOLVED);
        reviewRequestRepository.save(reviewRequest);
      }
    } else if (request.action() == ExceptionResolutionAction.APPROVE_AS_IS) {
      if (timeEntry != null) {
        timeEntry.setStatus(TimeEntryStatus.APPROVED);
        timeEntryRepository.save(timeEntry);
      }
      if (reviewRequest != null) {
        reviewRequest.setStatus(ReviewRequestStatus.RESOLVED);
        reviewRequestRepository.save(reviewRequest);
      }
    }

    MembershipEntity resolver = membershipRepository.findByCompanyIdAndId(companyId, tenantContext.requireMembershipId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Membership not found"));
    exception.resolve(request.action(), request.reason(), request.note(), resolver, Instant.now());
    exceptionRepository.save(exception);

    String workerName = workerProfileRepository.findByCompanyIdAndMembershipId(companyId, exception.getWorkerMembership().getId())
        .map(worker -> worker.getDisplayName())
        .orElse(exception.getWorkerMembership().getIdentity().getPhoneE164());
    return new ExceptionResponse(
        exception.getId(),
        exception.getType(),
        exception.getStatus(),
        crew.getId(),
        crew.getName(),
        exception.getWorkerMembership().getId(),
        workerName,
        timeEntry == null ? null : timeEntry.getId(),
        reviewRequest == null ? null : reviewRequest.getId(),
        timeEntry == null ? null : timeEntry.getCheckInAt(),
        timeEntry == null ? null : timeEntry.getCheckOutAt(),
        reviewRequest == null ? null : reviewRequest.getReason(),
        reviewRequest == null ? null : reviewRequest.getNote()
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
