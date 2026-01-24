package com.crewhandshake.features.foreman.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.security.TokenService;
import com.crewhandshake.common.tenant.DispatchAuthority;
import com.crewhandshake.common.tenant.TenantContext;
import com.crewhandshake.common.time.TimeParser;
import com.crewhandshake.config.AppProperties;
import com.crewhandshake.features.admin.persistence.CrewEntity;
import com.crewhandshake.features.admin.persistence.CrewRepository;
import com.crewhandshake.features.admin.persistence.ForemanProfileEntity;
import com.crewhandshake.features.admin.persistence.ForemanProfileRepository;
import com.crewhandshake.features.admin.persistence.SiteEntity;
import com.crewhandshake.features.admin.persistence.SiteRepository;
import com.crewhandshake.features.admin.persistence.WorkerProfileEntity;
import com.crewhandshake.features.admin.persistence.WorkerProfileRepository;
import com.crewhandshake.features.auth.persistence.CompanyEntity;
import com.crewhandshake.features.auth.persistence.CompanyRepository;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import com.crewhandshake.features.auth.persistence.MembershipRepository;
import com.crewhandshake.features.foreman.api.CrewCallCreateRequest;
import com.crewhandshake.features.foreman.api.CrewCallRecipientStatus;
import com.crewhandshake.features.foreman.api.CrewCallResponse;
import com.crewhandshake.features.foreman.api.CrewCallSummaryResponse;
import com.crewhandshake.features.foreman.api.CrewCallUpdateRequest;
import com.crewhandshake.features.foreman.api.ForemanCrewSummary;
import com.crewhandshake.features.foreman.api.SiteSummary;
import com.crewhandshake.features.foreman.persistence.CrewCallEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRepository;
import com.crewhandshake.features.foreman.persistence.CrewCallStatus;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientRepository;
import com.crewhandshake.features.foreman.persistence.RecipientSendStatus;
import com.crewhandshake.features.messaging.service.SmsProvider;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrewCallService {
  private static final DateTimeFormatter SMS_TIME = DateTimeFormatter.ofPattern("EEE MMM d, h:mm a");
  private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");
  private static final long TOKEN_TTL_DAYS = 30;

  private final TenantContext tenantContext;
  private final CompanyRepository companyRepository;
  private final CrewRepository crewRepository;
  private final SiteRepository siteRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final CrewCallRepository crewCallRepository;
  private final CrewCallRecipientRepository crewCallRecipientRepository;
  private final MembershipRepository membershipRepository;
  private final ForemanProfileRepository foremanProfileRepository;
  private final SmsProvider smsProvider;
  private final TokenService tokenService;
  private final TimeParser timeParser;
  private final AppProperties appProperties;

  public CrewCallService(TenantContext tenantContext,
                         CompanyRepository companyRepository,
                         CrewRepository crewRepository,
                         SiteRepository siteRepository,
                         WorkerProfileRepository workerProfileRepository,
                         CrewCallRepository crewCallRepository,
                         CrewCallRecipientRepository crewCallRecipientRepository,
                         MembershipRepository membershipRepository,
                         ForemanProfileRepository foremanProfileRepository,
                         SmsProvider smsProvider,
                         TokenService tokenService,
                         TimeParser timeParser,
                         AppProperties appProperties) {
    this.tenantContext = tenantContext;
    this.companyRepository = companyRepository;
    this.crewRepository = crewRepository;
    this.siteRepository = siteRepository;
    this.workerProfileRepository = workerProfileRepository;
    this.crewCallRepository = crewCallRepository;
    this.crewCallRecipientRepository = crewCallRecipientRepository;
    this.membershipRepository = membershipRepository;
    this.foremanProfileRepository = foremanProfileRepository;
    this.smsProvider = smsProvider;
    this.tokenService = tokenService;
    this.timeParser = timeParser;
    this.appProperties = appProperties;
  }

  @Transactional(readOnly = true)
  public List<ForemanCrewSummary> getForemanCrews() {
    tenantContext.requireForemanOrAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    List<CrewEntity> crews;
    if (tenantContext.isAdmin()) {
      crews = crewRepository.findByCompanyId(companyId);
    } else {
      UUID membershipId = tenantContext.requireMembershipId();
      crews = crewRepository.findByCompanyIdAndForemanMembershipId(companyId, membershipId);
    }
    return crews.stream()
        .map(crew -> new ForemanCrewSummary(crew.getId(), crew.getName()))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<SiteSummary> getCompanySites() {
    tenantContext.requireForemanOrAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    return siteRepository.findByCompanyId(companyId).stream()
        .filter(SiteEntity::isActive)
        .map(site -> new SiteSummary(site.getId(), site.getName(), site.getAddress()))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<CrewCallSummaryResponse> getCrewCalls(String date, UUID crewId) {
    tenantContext.requireForemanOrAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    CrewEntity crew = requireCrew(companyId, crewId);
    ensureCrewScope(crew);
    LocalDate workDate = timeParser.parseDate(date, "date");
    return crewCallRepository.findByCompanyIdAndCrewIdAndWorkDate(companyId, crew.getId(), workDate).stream()
        .map(call -> new CrewCallSummaryResponse(
            call.getId(),
            call.getSite().getName(),
            call.getStartAt(),
            call.getMeetPoint(),
            resolveSenderName(call.getSentByMembership()),
            call.getStatus()
        ))
        .collect(Collectors.toList());
  }

  @Transactional
  public CrewCallResponse createCrewCall(CrewCallCreateRequest request) {
    tenantContext.requireForemanOrAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    CompanyEntity company = requireCompany(companyId);
    CrewEntity crew = requireCrew(companyId, request.crewId());
    ensureCrewScope(crew);
    SiteEntity site = requireSite(companyId, request.siteId());
    if (!site.isActive()) {
      throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Site is inactive",
          java.util.Map.of("siteId", "Site must be active"));
    }
    if (company.getDispatchAuthority() != DispatchAuthority.HYBRID) {
      throw new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Dispatch not permitted");
    }
    Instant startAt = timeParser.parseInstant(request.startAt(), "startAt");
    LocalDate workDate = ZonedDateTime.ofInstant(startAt, DEFAULT_ZONE).toLocalDate();
    MembershipEntity sender = requireMembership(companyId, tenantContext.requireMembershipId());

    CrewCallEntity crewCall = new CrewCallEntity(
        company,
        crew,
        site,
        startAt,
        workDate,
        request.meetPoint().trim(),
        sender,
        CrewCallStatus.ACTIVE,
        Instant.now()
    );
    CrewCallEntity saved = crewCallRepository.save(crewCall);

    List<WorkerProfileEntity> workers = workerProfileRepository.findByCompanyIdAndCrewId(companyId, crew.getId())
        .stream()
        .filter(WorkerProfileEntity::isActive)
        .collect(Collectors.toList());

    List<CrewCallRecipientStatus> recipients = new ArrayList<>();
    for (WorkerProfileEntity worker : workers) {
      TokenService.TokenPair token = tokenService.createToken();
      Instant expiresAt = Instant.now().plusSeconds(TOKEN_TTL_DAYS * 24 * 3600);
      String message = buildCrewCallMessage(company, crew, site, startAt, request.meetPoint(), token.token(), sender);
      RecipientSendStatus sendStatus = RecipientSendStatus.SENT;
      String sendError = null;
      try {
        smsProvider.sendCrewCall(worker.getMembership().getIdentity().getPhoneE164(), message);
      } catch (Exception ex) {
        sendStatus = RecipientSendStatus.FAILED;
        sendError = "SMS failed";
      }
      CrewCallRecipientEntity recipient = new CrewCallRecipientEntity(
          company,
          saved,
          worker.getMembership(),
          token.hash(),
          expiresAt,
          sendStatus,
          sendError,
          Instant.now()
      );
      crewCallRecipientRepository.save(recipient);
      recipients.add(new CrewCallRecipientStatus(
          worker.getMembershipId(),
          worker.getDisplayName(),
          worker.getMembership().getIdentity().getPhoneE164(),
          sendStatus,
          sendError
      ));
    }

    return new CrewCallResponse(saved.getId(), saved.getStatus(), recipients);
  }

  @Transactional
  public CrewCallResponse resendCrewCall(UUID crewCallId, CrewCallUpdateRequest request) {
    tenantContext.requireForemanOrAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    CrewCallEntity crewCall = crewCallRepository.findByCompanyIdAndId(companyId, crewCallId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Crew call not found"));
    ensureCrewScope(crewCall.getCrew());
    SiteEntity site = requireSite(companyId, request.siteId());
    if (!site.isActive()) {
      throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Site is inactive",
          java.util.Map.of("siteId", "Site must be active"));
    }
    Instant startAt = timeParser.parseInstant(request.startAt(), "startAt");
    crewCall.setSite(site);
    crewCall.setStartAt(startAt);
    crewCall.setMeetPoint(request.meetPoint().trim());
    crewCallRepository.save(crewCall);

    List<CrewCallRecipientEntity> recipients = crewCallRecipientRepository.findByCompanyIdAndCrewCallId(companyId, crewCall.getId());
    List<CrewCallRecipientStatus> statuses = new ArrayList<>();
    for (CrewCallRecipientEntity recipient : recipients) {
      TokenService.TokenPair token = tokenService.createToken();
      recipient.setTokenHash(token.hash());
      recipient.setExpiresAt(Instant.now().plusSeconds(TOKEN_TTL_DAYS * 24 * 3600));
      String message = buildCrewCallMessage(
          crewCall.getCompany(),
          crewCall.getCrew(),
          site,
          startAt,
          crewCall.getMeetPoint(),
          token.token(),
          crewCall.getSentByMembership()
      );
      RecipientSendStatus sendStatus = RecipientSendStatus.SENT;
      String sendError = null;
      try {
        smsProvider.sendCrewCallUpdate(recipient.getWorkerMembership().getIdentity().getPhoneE164(), message);
      } catch (Exception ex) {
        sendStatus = RecipientSendStatus.FAILED;
        sendError = "SMS failed";
      }
      recipient.setSendStatus(sendStatus);
      recipient.setSendError(sendError);
      crewCallRecipientRepository.save(recipient);
      statuses.add(new CrewCallRecipientStatus(
          recipient.getWorkerMembership().getId(),
          resolveWorkerName(recipient.getWorkerMembership()),
          recipient.getWorkerMembership().getIdentity().getPhoneE164(),
          sendStatus,
          sendError
      ));
    }

    return new CrewCallResponse(crewCall.getId(), crewCall.getStatus(), statuses);
  }

  private CompanyEntity requireCompany(UUID companyId) {
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Company not found"));
  }

  private CrewEntity requireCrew(UUID companyId, UUID crewId) {
    return crewRepository.findByCompanyIdAndId(companyId, crewId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Crew not found"));
  }

  private SiteEntity requireSite(UUID companyId, UUID siteId) {
    return siteRepository.findByCompanyIdAndId(companyId, siteId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Site not found"));
  }

  private MembershipEntity requireMembership(UUID companyId, UUID membershipId) {
    return membershipRepository.findByCompanyIdAndId(companyId, membershipId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Membership not found"));
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

  private String buildCrewCallMessage(CompanyEntity company,
                                      CrewEntity crew,
                                      SiteEntity site,
                                      Instant startAt,
                                      String meetPoint,
                                      String token,
                                      MembershipEntity sender) {
    String baseUrl = appProperties.publicBaseUrl();
    String link = token == null ? "" : baseUrl.replaceAll("/+$", "") + "/w/t/" + token;
    String timeLabel = SMS_TIME.format(ZonedDateTime.ofInstant(startAt, DEFAULT_ZONE));
    String senderName = resolveSenderName(sender);
    StringBuilder message = new StringBuilder();
    message.append(company.getName()).append(": ");
    message.append(site.getName()).append(" at ").append(timeLabel).append(". ");
    message.append("Meet: ").append(meetPoint).append(". ");
    message.append("From ").append(senderName).append(". ");
    if (!link.isBlank()) {
      message.append(link);
    }
    return message.toString();
  }

  private String resolveSenderName(MembershipEntity membership) {
    return foremanProfileRepository.findByCompanyIdAndMembershipId(membership.getCompany().getId(), membership.getId())
        .map(ForemanProfileEntity::getDisplayName)
        .orElse(membership.getIdentity().getPhoneE164());
  }

  private String resolveWorkerName(MembershipEntity membership) {
    return workerProfileRepository.findByCompanyIdAndMembershipId(membership.getCompany().getId(), membership.getId())
        .map(WorkerProfileEntity::getDisplayName)
        .orElse(membership.getIdentity().getPhoneE164());
  }
}
