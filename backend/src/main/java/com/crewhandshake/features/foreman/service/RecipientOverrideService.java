package com.crewhandshake.features.foreman.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.security.TokenService;
import com.crewhandshake.common.tenant.TenantContext;
import com.crewhandshake.common.time.TimeParser;
import com.crewhandshake.config.AppProperties;
import com.crewhandshake.features.admin.persistence.CrewEntity;
import com.crewhandshake.features.admin.persistence.CrewRepository;
import com.crewhandshake.features.admin.persistence.SiteEntity;
import com.crewhandshake.features.admin.persistence.SiteRepository;
import com.crewhandshake.features.admin.persistence.WorkerProfileRepository;
import com.crewhandshake.features.foreman.api.CrewCallRecipientStatus;
import com.crewhandshake.features.foreman.api.RecipientOverrideRequest;
import com.crewhandshake.features.foreman.persistence.CrewCallEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientRepository;
import com.crewhandshake.features.foreman.persistence.CrewCallRepository;
import com.crewhandshake.features.foreman.persistence.RecipientSendStatus;
import com.crewhandshake.features.messaging.service.SmsProvider;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipientOverrideService {
  private static final DateTimeFormatter SMS_TIME = DateTimeFormatter.ofPattern("EEE MMM d, h:mm a");
  private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");
  private static final long TOKEN_TTL_DAYS = 30;

  private final TenantContext tenantContext;
  private final CrewCallRepository crewCallRepository;
  private final CrewCallRecipientRepository crewCallRecipientRepository;
  private final CrewRepository crewRepository;
  private final SiteRepository siteRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final SmsProvider smsProvider;
  private final TokenService tokenService;
  private final TimeParser timeParser;
  private final AppProperties appProperties;

  public RecipientOverrideService(TenantContext tenantContext,
                                  CrewCallRepository crewCallRepository,
                                  CrewCallRecipientRepository crewCallRecipientRepository,
                                  CrewRepository crewRepository,
                                  SiteRepository siteRepository,
                                  WorkerProfileRepository workerProfileRepository,
                                  SmsProvider smsProvider,
                                  TokenService tokenService,
                                  TimeParser timeParser,
                                  AppProperties appProperties) {
    this.tenantContext = tenantContext;
    this.crewCallRepository = crewCallRepository;
    this.crewCallRecipientRepository = crewCallRecipientRepository;
    this.crewRepository = crewRepository;
    this.siteRepository = siteRepository;
    this.workerProfileRepository = workerProfileRepository;
    this.smsProvider = smsProvider;
    this.tokenService = tokenService;
    this.timeParser = timeParser;
    this.appProperties = appProperties;
  }

  @Transactional
  public CrewCallRecipientStatus applyOverride(RecipientOverrideRequest request) {
    tenantContext.requireForemanOrAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    CrewCallEntity crewCall = crewCallRepository.findByCompanyIdAndId(companyId, request.crewCallId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Crew call not found"));
    CrewEntity crew = crewRepository.findByCompanyIdAndId(companyId, crewCall.getCrew().getId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Crew not found"));
    ensureCrewScope(crew);

    CrewCallRecipientEntity recipient = crewCallRecipientRepository
        .findByCompanyIdAndCrewCallIdAndWorkerMembershipId(companyId, crewCall.getId(), request.workerMembershipId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Recipient not found"));

    SiteEntity site = siteRepository.findByCompanyIdAndId(companyId, request.siteId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Site not found"));

    Instant startAt = timeParser.parseInstant(request.startAt(), "startAt");
    recipient.setOverrideSite(site);
    recipient.setOverrideStartAt(startAt);
    recipient.setOverrideMeetPoint(request.meetPoint().trim());

    TokenService.TokenPair token = tokenService.createToken();
    recipient.setTokenHash(token.hash());
    recipient.setExpiresAt(Instant.now().plusSeconds(TOKEN_TTL_DAYS * 24 * 3600));

    String link = appProperties.publicBaseUrl().replaceAll("/+$", "") + "/w/t/" + token.token();
    String timeLabel = SMS_TIME.format(ZonedDateTime.ofInstant(startAt, DEFAULT_ZONE));
    String message = crewCall.getCompany().getName() + ": Update - " + site.getName() + " at " + timeLabel +
        ". Meet: " + recipient.getOverrideMeetPoint() + ". " + link;

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

    String workerName = workerProfileRepository.findByCompanyIdAndMembershipId(companyId, recipient.getWorkerMembership().getId())
        .map(worker -> worker.getDisplayName())
        .orElse(recipient.getWorkerMembership().getIdentity().getPhoneE164());

    return new CrewCallRecipientStatus(
        recipient.getWorkerMembership().getId(),
        workerName,
        recipient.getWorkerMembership().getIdentity().getPhoneE164(),
        sendStatus,
        sendError
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
