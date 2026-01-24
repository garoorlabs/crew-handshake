package com.crewhandshake.features.auth.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.security.AuthSession;
import com.crewhandshake.common.security.MembershipRole;
import com.crewhandshake.common.security.SessionService;
import com.crewhandshake.common.security.TokenService;
import com.crewhandshake.config.AppProperties;
import com.crewhandshake.config.SeedProperties;
import com.crewhandshake.features.admin.persistence.SiteEntity;
import com.crewhandshake.features.admin.persistence.SiteRepository;
import com.crewhandshake.features.admin.persistence.WorkerProfileEntity;
import com.crewhandshake.features.admin.persistence.WorkerProfileRepository;
import com.crewhandshake.features.auth.api.DevWorkerLinkResponse;
import com.crewhandshake.features.auth.api.MeResponse;
import com.crewhandshake.features.auth.persistence.CompanyEntity;
import com.crewhandshake.features.auth.persistence.CompanyRepository;
import com.crewhandshake.features.auth.persistence.IdentityEntity;
import com.crewhandshake.features.auth.persistence.IdentityRepository;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import com.crewhandshake.features.auth.persistence.MembershipRepository;
import com.crewhandshake.features.foreman.persistence.CrewCallEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientRepository;
import com.crewhandshake.features.foreman.persistence.CrewCallRepository;
import com.crewhandshake.features.foreman.persistence.CrewCallStatus;
import com.crewhandshake.features.foreman.persistence.RecipientSendStatus;
import com.crewhandshake.features.messaging.service.SmsProvider;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevAuthService {
  private static final long TOKEN_TTL_DAYS = 30;

  private final AppProperties appProperties;
  private final SeedProperties seedProperties;
  private final PhoneNormalizer phoneNormalizer;
  private final IdentityRepository identityRepository;
  private final MembershipRepository membershipRepository;
  private final SessionService sessionService;
  private final MeService meService;
  private final CompanyRepository companyRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final CrewCallRepository crewCallRepository;
  private final CrewCallRecipientRepository crewCallRecipientRepository;
  private final SiteRepository siteRepository;
  private final TokenService tokenService;
  private final SmsProvider smsProvider;

  public DevAuthService(AppProperties appProperties,
                        SeedProperties seedProperties,
                        PhoneNormalizer phoneNormalizer,
                        IdentityRepository identityRepository,
                        MembershipRepository membershipRepository,
                        SessionService sessionService,
                        MeService meService,
                        CompanyRepository companyRepository,
                        WorkerProfileRepository workerProfileRepository,
                        CrewCallRepository crewCallRepository,
                        CrewCallRecipientRepository crewCallRecipientRepository,
                        SiteRepository siteRepository,
                        TokenService tokenService,
                        SmsProvider smsProvider) {
    this.appProperties = appProperties;
    this.seedProperties = seedProperties;
    this.phoneNormalizer = phoneNormalizer;
    this.identityRepository = identityRepository;
    this.membershipRepository = membershipRepository;
    this.sessionService = sessionService;
    this.meService = meService;
    this.companyRepository = companyRepository;
    this.workerProfileRepository = workerProfileRepository;
    this.crewCallRepository = crewCallRepository;
    this.crewCallRecipientRepository = crewCallRecipientRepository;
    this.siteRepository = siteRepository;
    this.tokenService = tokenService;
    this.smsProvider = smsProvider;
  }

  @Transactional
  public MeResponse login(String phoneRaw) {
    ensureDevAllowed();
    String phoneE164 = phoneNormalizer.normalize(phoneRaw);
    IdentityEntity identity = identityRepository.findByPhoneE164(phoneE164)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));
    List<MembershipEntity> memberships = membershipRepository.findByIdentityId(identity.getId());
    if (memberships.isEmpty()) {
      throw new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "No active membership");
    }
    MembershipEntity membership = memberships.stream()
        .sorted(Comparator.comparing((MembershipEntity m) -> m.getRoles().contains(MembershipRole.ADMIN) ? 0 : 1)
            .thenComparing(m -> m.getRoles().contains(MembershipRole.FOREMAN) ? 0 : 1))
        .findFirst()
        .orElseThrow(() -> new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "No active membership"));

    AuthSession session = new AuthSession(
        identity.getId(),
        identity.getPhoneE164(),
        membership.getCompany().getId(),
        membership.getId(),
        membership.getRoles()
    );
    sessionService.saveSession(session);
    return meService.getMe();
  }

  @Transactional
  public DevWorkerLinkResponse createWorkerLink() {
    ensureDevAllowed();

    String workerPhone = seedProperties.getWorkerPhone();
    IdentityEntity workerIdentity = identityRepository.findByPhoneE164(workerPhone)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Worker not found"));
    MembershipEntity workerMembership = membershipRepository.findByIdentityId(workerIdentity.getId()).stream()
        .filter(membership -> membership.getRoles().contains(MembershipRole.WORKER))
        .findFirst()
        .orElseThrow(() -> new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Worker membership missing"));

    CompanyEntity company = workerMembership.getCompany();
    WorkerProfileEntity workerProfile = workerProfileRepository.findByCompanyIdAndMembershipId(company.getId(), workerMembership.getId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Worker profile missing"));

    if (workerProfile.getCrew() == null) {
      throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Worker crew not set");
    }

    SiteEntity site = siteRepository.findByCompanyId(company.getId()).stream()
        .filter(SiteEntity::isActive)
        .findFirst()
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Active site missing"));

    MembershipEntity sender = resolveSender(company);

    Instant startAt = Instant.now().plusSeconds(3600);
    LocalDate workDate = LocalDate.now(ZoneOffset.UTC);
    CrewCallEntity crewCall = new CrewCallEntity(
        company,
        workerProfile.getCrew(),
        site,
        startAt,
        workDate,
        "Main gate",
        sender,
        CrewCallStatus.ACTIVE,
        Instant.now()
    );
    CrewCallEntity savedCall = crewCallRepository.save(crewCall);

    TokenService.TokenPair token = tokenService.createToken();
    Instant expiresAt = Instant.now().plusSeconds(TOKEN_TTL_DAYS * 24 * 3600);
    CrewCallRecipientEntity recipient = new CrewCallRecipientEntity(
        company,
        savedCall,
        workerMembership,
        token.hash(),
        expiresAt,
        RecipientSendStatus.SENT,
        null,
        Instant.now()
    );
    crewCallRecipientRepository.save(recipient);

    String url = appProperties.publicBaseUrl().replaceAll("/+$", "") + "/w/t/" + token.token();
    smsProvider.sendCrewCall(workerMembership.getIdentity().getPhoneE164(), "Demo crew call: " + url);

    return new DevWorkerLinkResponse(url, token.token(), savedCall.getId(), workerMembership.getIdentity().getPhoneE164());
  }

  private MembershipEntity resolveSender(CompanyEntity company) {
    String adminPhone = seedProperties.getAdminPhone();
    IdentityEntity adminIdentity = identityRepository.findByPhoneE164(adminPhone)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Admin not found"));
    return membershipRepository.findByIdentityId(adminIdentity.getId()).stream()
        .filter(membership -> membership.getCompany().getId().equals(company.getId()))
        .filter(membership -> membership.getRoles().contains(MembershipRole.ADMIN))
        .findFirst()
        .orElseThrow(() -> new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Admin membership missing"));
  }

  private void ensureDevAllowed() {
    if (!seedProperties.isEnabled()) {
      throw new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Dev endpoints disabled");
    }
    String provider = appProperties.smsProvider();
    if (provider == null || !provider.equalsIgnoreCase("log")) {
      throw new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Dev endpoints disabled");
    }
  }
}
