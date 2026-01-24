package com.crewhandshake.features.admin.service;

import com.crewhandshake.common.errors.ApiErrorCode;
import com.crewhandshake.common.errors.ApiException;
import com.crewhandshake.common.security.MembershipRole;
import com.crewhandshake.common.tenant.DispatchAuthority;
import com.crewhandshake.common.tenant.TenantContext;
import com.crewhandshake.common.time.TimeParser;
import com.crewhandshake.features.admin.api.CrewCreateRequest;
import com.crewhandshake.features.admin.api.CrewResponse;
import com.crewhandshake.features.admin.api.CrewUpdateRequest;
import com.crewhandshake.features.admin.api.CrewWorkerSummary;
import com.crewhandshake.features.admin.api.ForemanCreateRequest;
import com.crewhandshake.features.admin.api.ForemanResponse;
import com.crewhandshake.features.admin.api.ForemanUpdateRequest;
import com.crewhandshake.features.admin.api.SettingsResponse;
import com.crewhandshake.features.admin.api.SettingsUpdateRequest;
import com.crewhandshake.features.admin.api.SiteCreateRequest;
import com.crewhandshake.features.admin.api.SiteResponse;
import com.crewhandshake.features.admin.api.SiteUpdateRequest;
import com.crewhandshake.features.admin.api.WorkerCreateRequest;
import com.crewhandshake.features.admin.api.WorkerResponse;
import com.crewhandshake.features.admin.api.WorkerUpdateRequest;
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
import com.crewhandshake.features.auth.persistence.IdentityEntity;
import com.crewhandshake.features.auth.persistence.IdentityRepository;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import com.crewhandshake.features.auth.persistence.MembershipRepository;
import com.crewhandshake.features.auth.service.PhoneNormalizer;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
  private final TenantContext tenantContext;
  private final CompanyRepository companyRepository;
  private final IdentityRepository identityRepository;
  private final MembershipRepository membershipRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final ForemanProfileRepository foremanProfileRepository;
  private final CrewRepository crewRepository;
  private final SiteRepository siteRepository;
  private final PhoneNormalizer phoneNormalizer;
  private final TimeParser timeParser;

  public AdminService(TenantContext tenantContext,
                      CompanyRepository companyRepository,
                      IdentityRepository identityRepository,
                      MembershipRepository membershipRepository,
                      WorkerProfileRepository workerProfileRepository,
                      ForemanProfileRepository foremanProfileRepository,
                      CrewRepository crewRepository,
                      SiteRepository siteRepository,
                      PhoneNormalizer phoneNormalizer,
                      TimeParser timeParser) {
    this.tenantContext = tenantContext;
    this.companyRepository = companyRepository;
    this.identityRepository = identityRepository;
    this.membershipRepository = membershipRepository;
    this.workerProfileRepository = workerProfileRepository;
    this.foremanProfileRepository = foremanProfileRepository;
    this.crewRepository = crewRepository;
    this.siteRepository = siteRepository;
    this.phoneNormalizer = phoneNormalizer;
    this.timeParser = timeParser;
  }

  @Transactional(readOnly = true)
  public List<WorkerResponse> getWorkers() {
    CompanyEntity company = requireAdminCompany();
    return workerProfileRepository.findByCompanyId(company.getId()).stream()
        .map(worker -> new WorkerResponse(
            worker.getMembershipId(),
            worker.getDisplayName(),
            worker.getMembership().getIdentity().getPhoneE164(),
            worker.getPreferredLanguage(),
            worker.isActive(),
            worker.getCrew() == null ? null : worker.getCrew().getId(),
            worker.getCrew() == null ? null : worker.getCrew().getName()
        ))
        .collect(Collectors.toList());
  }

  @Transactional
  public WorkerResponse createWorker(WorkerCreateRequest request) {
    CompanyEntity company = requireAdminCompany();
    String phoneE164 = phoneNormalizer.normalize(request.phone());
    IdentityEntity identity = identityRepository.findByPhoneE164(phoneE164)
        .orElseGet(() -> identityRepository.save(new IdentityEntity(phoneE164)));

    MembershipEntity membership = membershipRepository.findByIdentityIdAndCompanyId(identity.getId(), company.getId())
        .orElseGet(() -> membershipRepository.save(new MembershipEntity(company, identity, Set.of(MembershipRole.WORKER))));

    if (!membership.getRoles().contains(MembershipRole.WORKER)) {
      membership.getRoles().add(MembershipRole.WORKER);
    }

    Optional<WorkerProfileEntity> existing = workerProfileRepository.findByCompanyIdAndMembershipId(company.getId(), membership.getId());
    if (existing.isPresent()) {
      throw new ApiException(ApiErrorCode.CONFLICT, HttpStatus.CONFLICT, "Worker already exists");
    }

    String language = request.preferredLanguage() == null || request.preferredLanguage().isBlank()
        ? company.getDefaultLanguage()
        : request.preferredLanguage().trim();
    boolean active = request.active() == null || request.active();

    WorkerProfileEntity worker = new WorkerProfileEntity(membership, company, request.displayName().trim(), language, active);
    if (request.crewId() != null) {
      CrewEntity crew = requireCrew(company.getId(), request.crewId());
      if (!worker.isActive()) {
        throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Inactive workers cannot be assigned",
            java.util.Map.of("crewId", "Worker must be active to assign to a crew"));
      }
      worker.setCrew(crew);
    }
    WorkerProfileEntity saved = workerProfileRepository.save(worker);

    return new WorkerResponse(
        saved.getMembershipId(),
        saved.getDisplayName(),
        membership.getIdentity().getPhoneE164(),
        saved.getPreferredLanguage(),
        saved.isActive(),
        saved.getCrew() == null ? null : saved.getCrew().getId(),
        saved.getCrew() == null ? null : saved.getCrew().getName()
    );
  }

  @Transactional
  public WorkerResponse updateWorker(WorkerUpdateRequest request) {
    CompanyEntity company = requireAdminCompany();
    WorkerProfileEntity worker = workerProfileRepository.findByCompanyIdAndMembershipId(company.getId(), request.membershipId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Worker not found"));

    worker.setDisplayName(request.displayName().trim());
    if (request.preferredLanguage() != null && !request.preferredLanguage().isBlank()) {
      worker.setPreferredLanguage(request.preferredLanguage().trim());
    }
    if (request.active() != null) {
      worker.setActive(request.active());
    }

    if (request.crewId() != null) {
      if (!worker.isActive()) {
        throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Inactive workers cannot be assigned",
            java.util.Map.of("crewId", "Worker must be active to assign to a crew"));
      }
      CrewEntity crew = requireCrew(company.getId(), request.crewId());
      worker.setCrew(crew);
    } else {
      worker.setCrew(null);
    }

    WorkerProfileEntity saved = workerProfileRepository.save(worker);
    return new WorkerResponse(
        saved.getMembershipId(),
        saved.getDisplayName(),
        saved.getMembership().getIdentity().getPhoneE164(),
        saved.getPreferredLanguage(),
        saved.isActive(),
        saved.getCrew() == null ? null : saved.getCrew().getId(),
        saved.getCrew() == null ? null : saved.getCrew().getName()
    );
  }

  @Transactional(readOnly = true)
  public List<ForemanResponse> getForemen() {
    CompanyEntity company = requireAdminCompany();
    return foremanProfileRepository.findByCompanyId(company.getId()).stream()
        .map(foreman -> new ForemanResponse(
            foreman.getMembershipId(),
            foreman.getDisplayName(),
            foreman.getMembership().getIdentity().getPhoneE164(),
            foreman.isActive()
        ))
        .collect(Collectors.toList());
  }

  @Transactional
  public ForemanResponse createForeman(ForemanCreateRequest request) {
    CompanyEntity company = requireAdminCompany();
    String phoneE164 = phoneNormalizer.normalize(request.phone());
    IdentityEntity identity = identityRepository.findByPhoneE164(phoneE164)
        .orElseGet(() -> identityRepository.save(new IdentityEntity(phoneE164)));

    MembershipEntity membership = membershipRepository.findByIdentityIdAndCompanyId(identity.getId(), company.getId())
        .orElseGet(() -> membershipRepository.save(new MembershipEntity(company, identity, Set.of(MembershipRole.FOREMAN))));

    if (!membership.getRoles().contains(MembershipRole.FOREMAN)) {
      membership.getRoles().add(MembershipRole.FOREMAN);
    }

    Optional<ForemanProfileEntity> existing = foremanProfileRepository.findByCompanyIdAndMembershipId(company.getId(), membership.getId());
    if (existing.isPresent()) {
      throw new ApiException(ApiErrorCode.CONFLICT, HttpStatus.CONFLICT, "Foreman already exists");
    }

    boolean active = request.active() == null || request.active();
    ForemanProfileEntity foreman = new ForemanProfileEntity(membership, company, request.displayName().trim(), active);
    ForemanProfileEntity saved = foremanProfileRepository.save(foreman);

    return new ForemanResponse(
        saved.getMembershipId(),
        saved.getDisplayName(),
        membership.getIdentity().getPhoneE164(),
        saved.isActive()
    );
  }

  @Transactional
  public ForemanResponse updateForeman(ForemanUpdateRequest request) {
    CompanyEntity company = requireAdminCompany();
    ForemanProfileEntity foreman = foremanProfileRepository.findByCompanyIdAndMembershipId(company.getId(), request.membershipId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Foreman not found"));

    foreman.setDisplayName(request.displayName().trim());
    if (request.active() != null) {
      foreman.setActive(request.active());
    }
    ForemanProfileEntity saved = foremanProfileRepository.save(foreman);

    return new ForemanResponse(
        saved.getMembershipId(),
        saved.getDisplayName(),
        saved.getMembership().getIdentity().getPhoneE164(),
        saved.isActive()
    );
  }

  @Transactional(readOnly = true)
  public List<CrewResponse> getCrews() {
    CompanyEntity company = requireAdminCompany();
    List<CrewEntity> crews = crewRepository.findByCompanyId(company.getId());
    List<WorkerProfileEntity> workers = workerProfileRepository.findByCompanyId(company.getId());

    return crews.stream().map(crew -> {
      List<CrewWorkerSummary> crewWorkers = workers.stream()
          .filter(worker -> worker.getCrew() != null && crew.getId().equals(worker.getCrew().getId()))
          .map(worker -> new CrewWorkerSummary(worker.getMembershipId(), worker.getDisplayName()))
          .collect(Collectors.toList());
      String foremanName = foremanProfileRepository.findByCompanyIdAndMembershipId(company.getId(), crew.getForemanMembership().getId())
          .map(ForemanProfileEntity::getDisplayName)
          .orElse("Foreman");
      return new CrewResponse(
          crew.getId(),
          crew.getName(),
          crew.getForemanMembership().getId(),
          foremanName,
          crewWorkers.size(),
          crewWorkers
      );
    }).collect(Collectors.toList());
  }

  @Transactional
  public CrewResponse createCrew(CrewCreateRequest request) {
    CompanyEntity company = requireAdminCompany();
    MembershipEntity foremanMembership = requireMembership(company.getId(), request.foremanMembershipId());
    if (!foremanMembership.getRoles().contains(MembershipRole.FOREMAN)) {
      throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Foreman required",
          java.util.Map.of("foremanMembershipId", "Selected membership is not a foreman"));
    }
    CrewEntity crew = new CrewEntity(company, request.name().trim(), foremanMembership);
    CrewEntity saved = crewRepository.save(crew);

    List<UUID> workerIds = request.workerMembershipIds() == null ? List.of() : request.workerMembershipIds();
    assignWorkersToCrew(company, saved, workerIds);
    return mapCrew(company, saved);
  }

  @Transactional
  public CrewResponse updateCrew(CrewUpdateRequest request) {
    CompanyEntity company = requireAdminCompany();
    CrewEntity crew = crewRepository.findByCompanyIdAndId(company.getId(), request.crewId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Crew not found"));

    MembershipEntity foremanMembership = requireMembership(company.getId(), request.foremanMembershipId());
    if (!foremanMembership.getRoles().contains(MembershipRole.FOREMAN)) {
      throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Foreman required",
          java.util.Map.of("foremanMembershipId", "Selected membership is not a foreman"));
    }

    crew.setName(request.name().trim());
    crew.setForemanMembership(foremanMembership);
    crewRepository.save(crew);

    List<UUID> workerIds = request.workerMembershipIds() == null ? List.of() : request.workerMembershipIds();
    assignWorkersToCrew(company, crew, workerIds);
    return mapCrew(company, crew);
  }

  @Transactional(readOnly = true)
  public List<SiteResponse> getSites() {
    CompanyEntity company = requireAdminCompany();
    return siteRepository.findByCompanyId(company.getId()).stream()
        .map(site -> new SiteResponse(site.getId(), site.getName(), site.getAddress(), site.getNotes(), site.isActive()))
        .collect(Collectors.toList());
  }

  @Transactional
  public SiteResponse createSite(SiteCreateRequest request) {
    CompanyEntity company = requireAdminCompany();
    boolean active = request.active() == null || request.active();
    SiteEntity site = new SiteEntity(company, request.name().trim(), trimOrNull(request.address()), trimOrNull(request.notes()), active);
    SiteEntity saved = siteRepository.save(site);
    return new SiteResponse(saved.getId(), saved.getName(), saved.getAddress(), saved.getNotes(), saved.isActive());
  }

  @Transactional
  public SiteResponse updateSite(SiteUpdateRequest request) {
    CompanyEntity company = requireAdminCompany();
    SiteEntity site = siteRepository.findByCompanyIdAndId(company.getId(), request.siteId())
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Site not found"));

    site.setName(request.name().trim());
    site.setAddress(trimOrNull(request.address()));
    site.setNotes(trimOrNull(request.notes()));
    if (request.active() != null) {
      site.setActive(request.active());
    }
    SiteEntity saved = siteRepository.save(site);
    return new SiteResponse(saved.getId(), saved.getName(), saved.getAddress(), saved.getNotes(), saved.isActive());
  }

  @Transactional(readOnly = true)
  public SettingsResponse getSettings() {
    CompanyEntity company = requireAdminCompany();
    return new SettingsResponse(
        company.getDefaultLanguage(),
        company.getPayrollFrequency(),
        company.getPayrollCutoffDay(),
        company.getStandbyCutoffTime().toString(),
        company.getDispatchAuthority()
    );
  }

  @Transactional
  public SettingsResponse updateSettings(SettingsUpdateRequest request) {
    CompanyEntity company = requireAdminCompany();
    LocalTime standbyTime = timeParser.parseLocalTime(request.standbyCutoffTime(), "standbyCutoffTime");
    if (request.dispatchAuthority() != DispatchAuthority.HYBRID) {
      throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Only HYBRID is supported",
          java.util.Map.of("dispatchAuthority", "Only HYBRID is supported"));
    }
    company.setDefaultLanguage(request.defaultLanguage().trim());
    company.setPayrollFrequency(request.payrollFrequency());
    company.setPayrollCutoffDay(request.payrollCutoffDay());
    company.setStandbyCutoffTime(standbyTime);
    company.setDispatchAuthority(request.dispatchAuthority());
    companyRepository.save(company);
    return getSettings();
  }

  private CompanyEntity requireAdminCompany() {
    tenantContext.requireAdmin();
    UUID companyId = tenantContext.requireCompanyId();
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Company not found"));
  }

  private CrewEntity requireCrew(UUID companyId, UUID crewId) {
    return crewRepository.findByCompanyIdAndId(companyId, crewId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Crew not found"));
  }

  private MembershipEntity requireMembership(UUID companyId, UUID membershipId) {
    return membershipRepository.findByCompanyIdAndId(companyId, membershipId)
        .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Membership not found"));
  }

  private void assignWorkersToCrew(CompanyEntity company, CrewEntity crew, List<UUID> workerIds) {
    List<WorkerProfileEntity> workers = workerProfileRepository.findByCompanyId(company.getId());
    List<UUID> targetIds = workerIds == null ? List.of() : workerIds;

    for (WorkerProfileEntity worker : workers) {
      boolean shouldAssign = targetIds.contains(worker.getMembershipId());
      if (shouldAssign && !worker.isActive()) {
        throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Inactive worker cannot be assigned",
            java.util.Map.of("workerMembershipIds", "Inactive worker cannot be assigned"));
      }
      if (shouldAssign) {
        worker.setCrew(crew);
      } else if (worker.getCrew() != null && worker.getCrew().getId().equals(crew.getId())) {
        worker.setCrew(null);
      }
    }
    workerProfileRepository.saveAll(workers);
  }

  private CrewResponse mapCrew(CompanyEntity company, CrewEntity crew) {
    List<WorkerProfileEntity> workers = workerProfileRepository.findByCompanyIdAndCrewId(company.getId(), crew.getId());
    List<CrewWorkerSummary> crewWorkers = workers.stream()
        .map(worker -> new CrewWorkerSummary(worker.getMembershipId(), worker.getDisplayName()))
        .collect(Collectors.toList());
    String foremanName = foremanProfileRepository.findByCompanyIdAndMembershipId(company.getId(), crew.getForemanMembership().getId())
        .map(ForemanProfileEntity::getDisplayName)
        .orElse("Foreman");
    return new CrewResponse(
        crew.getId(),
        crew.getName(),
        crew.getForemanMembership().getId(),
        foremanName,
        crewWorkers.size(),
        crewWorkers
    );
  }

  private String trimOrNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
