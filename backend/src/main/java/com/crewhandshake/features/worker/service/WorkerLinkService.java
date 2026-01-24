package com.crewhandshake.features.worker.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.security.TokenService;
import com.crewhandshake.common.time.TimeParser;
import com.crewhandshake.features.admin.persistence.WorkerProfileEntity;
import com.crewhandshake.features.admin.persistence.WorkerProfileRepository;
import com.crewhandshake.features.foreman.persistence.CrewCallEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRepository;
import com.crewhandshake.features.foreman.persistence.CrewCallStatus;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientRepository;
import com.crewhandshake.features.foreman.persistence.ExceptionEntity;
import com.crewhandshake.features.foreman.persistence.ExceptionRepository;
import com.crewhandshake.features.foreman.persistence.ExceptionStatus;
import com.crewhandshake.features.foreman.persistence.ExceptionType;
import com.crewhandshake.features.foreman.persistence.HandshakeStatus;
import com.crewhandshake.features.foreman.persistence.ReviewRequestEntity;
import com.crewhandshake.features.foreman.persistence.ReviewRequestRepository;
import com.crewhandshake.features.foreman.persistence.ReviewRequestStatus;
import com.crewhandshake.features.foreman.persistence.TimeEntryEntity;
import com.crewhandshake.features.foreman.persistence.TimeEntryRepository;
import com.crewhandshake.features.foreman.persistence.TimeEntrySource;
import com.crewhandshake.features.foreman.persistence.TimeEntryStatus;
import com.crewhandshake.features.worker.api.WorkerAction;
import com.crewhandshake.features.worker.api.WorkerAvailabilityRequest;
import com.crewhandshake.features.worker.api.WorkerCheckInResponse;
import com.crewhandshake.features.worker.api.WorkerCheckOutResponse;
import com.crewhandshake.features.worker.api.WorkerCrewCallResponse;
import com.crewhandshake.features.worker.api.WorkerHandshakeRequest;
import com.crewhandshake.features.worker.api.WorkerLinkResolutionResponse;
import com.crewhandshake.features.worker.api.WorkerReviewRequestCreateRequest;
import com.crewhandshake.features.worker.api.WorkerReviewRequestResponse;
import com.crewhandshake.features.worker.api.WorkerTimecardEntry;
import com.crewhandshake.features.worker.api.WorkerTimecardResponse;
import com.crewhandshake.features.worker.persistence.WorkerTimecardLinkRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkerLinkService {
  private final TokenService tokenService;
  private final CrewCallRecipientRepository crewCallRecipientRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final TimeEntryRepository timeEntryRepository;
  private final ReviewRequestRepository reviewRequestRepository;
  private final CrewCallRepository crewCallRepository;
  private final TimeParser timeParser;
  private final WorkerTimecardLinkRepository workerTimecardLinkRepository;
  private final ExceptionRepository exceptionRepository;

  public WorkerLinkService(TokenService tokenService,
                           CrewCallRecipientRepository crewCallRecipientRepository,
                           WorkerProfileRepository workerProfileRepository,
                           TimeEntryRepository timeEntryRepository,
                           ReviewRequestRepository reviewRequestRepository,
                           CrewCallRepository crewCallRepository,
                           TimeParser timeParser,
                           WorkerTimecardLinkRepository workerTimecardLinkRepository,
                           ExceptionRepository exceptionRepository) {
    this.tokenService = tokenService;
    this.crewCallRecipientRepository = crewCallRecipientRepository;
    this.workerProfileRepository = workerProfileRepository;
    this.timeEntryRepository = timeEntryRepository;
    this.reviewRequestRepository = reviewRequestRepository;
    this.crewCallRepository = crewCallRepository;
    this.timeParser = timeParser;
    this.workerTimecardLinkRepository = workerTimecardLinkRepository;
    this.exceptionRepository = exceptionRepository;
  }

  @Transactional(readOnly = true)
  public WorkerLinkResolutionResponse resolveLink(String token) {
    String hash = tokenService.hashToken(token);
    CrewCallRecipientEntity recipient = crewCallRecipientRepository.findByTokenHash(hash).orElse(null);
    if (recipient != null && !isExpired(recipient)) {
      return new WorkerLinkResolutionResponse("CREW_CALL");
    }
    if (workerTimecardLinkRepository.findByTokenHash(hash).isPresent()) {
      return new WorkerLinkResolutionResponse("TIMECARD");
    }
    throw new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Link expired or invalid");
  }

  @Transactional(readOnly = true)
  public WorkerCrewCallResponse getCrewCall(String token) {
    CrewCallRecipientEntity recipient = requireRecipient(token);
    CrewCallEntity crewCall = recipient.getCrewCall();
    if (crewCall.getStatus() == CrewCallStatus.CANCELLED) {
      throw new ApiException(ApiErrorCode.CONFLICT, HttpStatus.CONFLICT, "Crew call cancelled");
    }
    TimeEntryEntity timeEntry = timeEntryRepository
        .findByCompanyIdAndWorkerMembershipIdAndCrewCallIdAndWorkDate(
            crewCall.getCompany().getId(),
            recipient.getWorkerMembership().getId(),
            crewCall.getId(),
            crewCall.getWorkDate())
        .orElse(null);

    WorkerAction primaryAction = WorkerAction.NONE;
    List<WorkerAction> actions = new ArrayList<>();
    boolean needsAvailability = recipient.getHandshakeStatus() == HandshakeStatus.CANT
        || recipient.getHandshakeStatus() == HandshakeStatus.NEED_CHANGE;

    if (timeEntry != null && timeEntry.getCheckOutAt() != null) {
      primaryAction = WorkerAction.VIEW_RECEIPT;
      actions.add(WorkerAction.VIEW_RECEIPT);
    } else if (timeEntry != null && timeEntry.getCheckInAt() != null) {
      primaryAction = WorkerAction.CHECK_OUT;
      actions.add(WorkerAction.CHECK_OUT);
    } else if (recipient.getHandshakeStatus() == null) {
      primaryAction = WorkerAction.CONFIRM;
      actions.add(WorkerAction.CONFIRM);
      actions.add(WorkerAction.LATE);
      actions.add(WorkerAction.CANT);
      actions.add(WorkerAction.NEED_CHANGE);
    } else if (recipient.getHandshakeStatus() == HandshakeStatus.CONFIRMED || recipient.getHandshakeStatus() == HandshakeStatus.LATE) {
      primaryAction = WorkerAction.CHECK_IN;
      actions.add(WorkerAction.CHECK_IN);
      needsAvailability = false;
    } else if (recipient.getAvailabilityAfter() != null) {
      needsAvailability = false;
    }

    String senderName = resolveSenderName(crewCall);

    return new WorkerCrewCallResponse(
        crewCall.getId(),
        crewCall.getCompany().getName(),
        crewCall.getCrew().getName(),
        recipient.getOverrideSite() == null ? crewCall.getSite().getName() : recipient.getOverrideSite().getName(),
        recipient.getOverrideSite() == null ? crewCall.getSite().getAddress() : recipient.getOverrideSite().getAddress(),
        recipient.getOverrideStartAt() == null ? crewCall.getStartAt() : recipient.getOverrideStartAt(),
        recipient.getOverrideMeetPoint() == null ? crewCall.getMeetPoint() : recipient.getOverrideMeetPoint(),
        senderName,
        recipient.getHandshakeStatus(),
        recipient.getLateEtaMinutes(),
        recipient.getAvailabilityAfter(),
        recipient.getAvailabilityDifferentSiteOk(),
        recipient.getAvailabilityNote(),
        timeEntry == null ? null : timeEntry.getCheckInAt(),
        timeEntry == null ? null : timeEntry.getCheckOutAt(),
        primaryAction,
        actions,
        token,
        needsAvailability
    );
  }

  @Transactional
  public WorkerCrewCallResponse submitHandshake(String token, WorkerHandshakeRequest request) {
    CrewCallRecipientEntity recipient = requireRecipient(token);
    if (recipient.getHandshakeStatus() != null) {
      throw new ApiException(ApiErrorCode.CONFLICT, HttpStatus.CONFLICT, "Already responded");
    }
    if (request.status() == HandshakeStatus.LATE && (request.lateEtaMinutes() == null || request.lateEtaMinutes() <= 0)) {
      throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Late ETA required",
          java.util.Map.of("lateEtaMinutes", "Select a delay window"));
    }
    recipient.setHandshakeStatus(request.status());
    recipient.setHandshakeAt(Instant.now());
    if (request.status() == HandshakeStatus.LATE) {
      recipient.setLateEtaMinutes(request.lateEtaMinutes());
    }
    crewCallRecipientRepository.save(recipient);
    return getCrewCall(token);
  }

  @Transactional
  public WorkerCrewCallResponse submitAvailability(String token, WorkerAvailabilityRequest request) {
    CrewCallRecipientEntity recipient = requireRecipient(token);
    if (recipient.getHandshakeStatus() != HandshakeStatus.CANT && recipient.getHandshakeStatus() != HandshakeStatus.NEED_CHANGE) {
      throw new ApiException(ApiErrorCode.CONFLICT, HttpStatus.CONFLICT, "Availability not allowed");
    }
    recipient.setAvailabilityAfter(request.availabilityAfter());
    recipient.setAvailabilityDifferentSiteOk(request.differentSiteOk());
    recipient.setAvailabilityNote(request.note());
    crewCallRecipientRepository.save(recipient);
    return getCrewCall(token);
  }

  @Transactional
  public WorkerCheckInResponse checkIn(String token) {
    CrewCallRecipientEntity recipient = requireRecipient(token);
    CrewCallEntity crewCall = recipient.getCrewCall();
    TimeEntryEntity timeEntry = timeEntryRepository
        .findByCompanyIdAndWorkerMembershipIdAndCrewCallIdAndWorkDate(
            crewCall.getCompany().getId(),
            recipient.getWorkerMembership().getId(),
            crewCall.getId(),
            crewCall.getWorkDate())
        .orElseGet(() -> new TimeEntryEntity(
            crewCall.getCompany(),
            recipient.getWorkerMembership(),
            crewCall,
            crewCall.getWorkDate(),
            TimeEntrySource.WORKER_LINK,
            TimeEntryStatus.PENDING,
            Instant.now()
        ));

    if (timeEntry.getCheckInAt() == null) {
      timeEntry.setCheckInAt(Instant.now());
      timeEntry.setSource(TimeEntrySource.WORKER_LINK);
      timeEntryRepository.save(timeEntry);
    }
    return new WorkerCheckInResponse(timeEntry.getCheckInAt());
  }

  @Transactional
  public WorkerCheckOutResponse checkOut(String token) {
    CrewCallRecipientEntity recipient = requireRecipient(token);
    CrewCallEntity crewCall = recipient.getCrewCall();
    TimeEntryEntity timeEntry = timeEntryRepository
        .findByCompanyIdAndWorkerMembershipIdAndCrewCallIdAndWorkDate(
            crewCall.getCompany().getId(),
            recipient.getWorkerMembership().getId(),
            crewCall.getId(),
            crewCall.getWorkDate())
        .orElseThrow(() -> new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Check-in required"));

    if (timeEntry.getCheckInAt() == null) {
      throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Check-in required");
    }
    if (timeEntry.getCheckOutAt() == null) {
      timeEntry.setCheckOutAt(Instant.now());
      timeEntryRepository.save(timeEntry);
    }
    return new WorkerCheckOutResponse(timeEntry.getCheckOutAt());
  }

  @Transactional(readOnly = true)
  public WorkerTimecardResponse getTimecard(String token, String week) {
    TokenResolution resolution = resolveToken(token);
    LocalDate weekStart = week == null ? LocalDate.now(ZoneOffset.UTC).with(java.time.DayOfWeek.MONDAY) : timeParser.parseDate(week, "week");
    LocalDate weekEnd = weekStart.plusDays(6);
    List<TimeEntryEntity> entries = timeEntryRepository.findByCompanyIdAndWorkerMembershipIdAndWorkDateBetween(
        resolution.companyId,
        resolution.workerMembershipId,
        weekStart,
        weekEnd
    );
    List<WorkerTimecardEntry> responseEntries = entries.stream()
        .map(entry -> {
          ReviewRequestEntity reviewRequest = reviewRequestRepository
              .findByCompanyIdAndWorkerMembershipIdAndWorkDate(resolution.companyId, resolution.workerMembershipId, entry.getWorkDate())
              .orElse(null);
          String reviewStatus = reviewRequest == null ? "NONE" : reviewRequest.getStatus().name();
          return new WorkerTimecardEntry(
              entry.getId(),
              entry.getWorkDate(),
              entry.getCrewCall().getCrew().getName(),
              entry.getCrewCall().getSite().getName(),
              entry.getCheckInAt(),
              entry.getCheckOutAt(),
              entry.getStatus(),
              entry.isEdited(),
              entry.getEditReason(),
              reviewStatus
          );
        })
        .collect(Collectors.toList());

    return new WorkerTimecardResponse(weekStart, weekEnd, responseEntries);
  }

  @Transactional
  public WorkerReviewRequestResponse submitReviewRequest(String token, WorkerReviewRequestCreateRequest request) {
    TokenResolution resolution = resolveToken(token);
    LocalDate workDate = timeParser.parseDate(request.workDate(), "workDate");
    ReviewRequestEntity existing = reviewRequestRepository
        .findByCompanyIdAndWorkerMembershipIdAndWorkDate(resolution.companyId, resolution.workerMembershipId, workDate)
        .orElse(null);
    if (existing != null) {
      throw new ApiException(ApiErrorCode.CONFLICT, HttpStatus.CONFLICT, "Review request already submitted");
    }

    ReviewRequestEntity reviewRequest = new ReviewRequestEntity(
        resolution.company,
        resolution.membership,
        workDate,
        request.reason().trim(),
        request.note(),
        ReviewRequestStatus.OPEN,
        Instant.now()
    );
    ReviewRequestEntity saved = reviewRequestRepository.save(reviewRequest);

    WorkerProfileEntity worker = workerProfileRepository.findByCompanyIdAndMembershipId(resolution.companyId, resolution.workerMembershipId)
        .orElse(null);
    TimeEntryEntity timeEntry = timeEntryRepository
        .findByCompanyIdAndWorkerMembershipIdAndWorkDate(resolution.companyId, resolution.workerMembershipId, workDate)
        .orElse(null);
    CrewCallEntity crewCall = timeEntry == null ? null : timeEntry.getCrewCall();
    if (timeEntry != null) {
      timeEntry.setStatus(TimeEntryStatus.NEEDS_REVIEW);
      timeEntryRepository.save(timeEntry);
    }
    if (crewCall == null && worker != null && worker.getCrew() != null) {
      crewCall = crewCallRepository
          .findTopByCompanyIdAndCrewIdAndWorkDateOrderByCreatedAtDesc(resolution.companyId, worker.getCrew().getId(), workDate)
          .orElse(null);
    }
    if (crewCall == null) {
      throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Crew call not found");
    }

    ExceptionEntity exception = new ExceptionEntity(
        resolution.company,
        crewCall.getCrew(),
        resolution.membership,
        ExceptionType.REVIEW_REQUEST,
        ExceptionStatus.OPEN,
        null,
        saved,
        Instant.now()
    );
    exceptionRepository.save(exception);

    return new WorkerReviewRequestResponse(saved.getId(), saved.getStatus().name(), saved.getCreatedAt());
  }

  private CrewCallRecipientEntity requireRecipient(String token) {
    String hash = tokenService.hashToken(token);
    CrewCallRecipientEntity recipient = crewCallRecipientRepository.findByTokenHash(hash)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Link expired or invalid"));
    if (isExpired(recipient)) {
      throw new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Link expired or invalid");
    }
    return recipient;
  }

  private boolean isExpired(CrewCallRecipientEntity recipient) {
    return recipient.getExpiresAt().isBefore(Instant.now());
  }

  private String resolveSenderName(CrewCallEntity crewCall) {
    return crewCall.getSentByMembership().getIdentity().getPhoneE164();
  }

  private TokenResolution resolveToken(String token) {
    String hash = tokenService.hashToken(token);
    CrewCallRecipientEntity recipient = crewCallRecipientRepository.findByTokenHash(hash).orElse(null);
    if (recipient != null && !isExpired(recipient)) {
      return new TokenResolution(recipient.getCompany().getId(), recipient.getCompany(), recipient.getWorkerMembership(), recipient.getWorkerMembership().getId());
    }
    return workerTimecardLinkRepository.findByTokenHash(hash)
        .map(link -> new TokenResolution(link.getCompany().getId(), link.getCompany(), link.getWorkerMembership(), link.getWorkerMembership().getId()))
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Link expired or invalid"));
  }

  private static class TokenResolution {
    private final UUID companyId;
    private final com.crewhandshake.features.auth.persistence.CompanyEntity company;
    private final com.crewhandshake.features.auth.persistence.MembershipEntity membership;
    private final UUID workerMembershipId;

    private TokenResolution(UUID companyId,
                            com.crewhandshake.features.auth.persistence.CompanyEntity company,
                            com.crewhandshake.features.auth.persistence.MembershipEntity membership,
                            UUID workerMembershipId) {
      this.companyId = companyId;
      this.company = company;
      this.membership = membership;
      this.workerMembershipId = workerMembershipId;
    }
  }
}
