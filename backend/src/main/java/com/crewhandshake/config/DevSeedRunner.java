package com.crewhandshake.config;

import com.crewhandshake.common.security.MembershipRole;
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
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DevSeedRunner implements ApplicationRunner {
  private static final Logger logger = LoggerFactory.getLogger(DevSeedRunner.class);

  private final SeedProperties seedProperties;
  private final DataSourceProperties dataSourceProperties;
  private final CompanyRepository companyRepository;
  private final IdentityRepository identityRepository;
  private final MembershipRepository membershipRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final ForemanProfileRepository foremanProfileRepository;
  private final CrewRepository crewRepository;
  private final SiteRepository siteRepository;

  public DevSeedRunner(SeedProperties seedProperties,
                       DataSourceProperties dataSourceProperties,
                       CompanyRepository companyRepository,
                       IdentityRepository identityRepository,
                       MembershipRepository membershipRepository,
                       WorkerProfileRepository workerProfileRepository,
                       ForemanProfileRepository foremanProfileRepository,
                       CrewRepository crewRepository,
                       SiteRepository siteRepository) {
    this.seedProperties = seedProperties;
    this.dataSourceProperties = dataSourceProperties;
    this.companyRepository = companyRepository;
    this.identityRepository = identityRepository;
    this.membershipRepository = membershipRepository;
    this.workerProfileRepository = workerProfileRepository;
    this.foremanProfileRepository = foremanProfileRepository;
    this.crewRepository = crewRepository;
    this.siteRepository = siteRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (!seedProperties.isEnabled()) {
      return;
    }
    if (!isInMemoryDatabase()) {
      logger.info("Dev seed skipped: non-H2 database detected.");
      return;
    }
    seedIfEmpty();
  }

  void seedIfEmpty() {
    if (companyRepository.count() > 0) {
      return;
    }

    CompanyEntity company = companyRepository.save(new CompanyEntity(seedProperties.getCompanyName()));

    IdentityEntity adminIdentity = findOrCreateIdentity(seedProperties.getAdminPhone());
    IdentityEntity foremanIdentity = findOrCreateIdentity(seedProperties.getForemanPhone());
    IdentityEntity workerIdentity = findOrCreateIdentity(seedProperties.getWorkerPhone());

    MembershipEntity adminMembership = membershipRepository.save(new MembershipEntity(
        company,
        adminIdentity,
        Set.of(MembershipRole.ADMIN)
    ));
    MembershipEntity foremanMembership = membershipRepository.save(new MembershipEntity(
        company,
        foremanIdentity,
        Set.of(MembershipRole.FOREMAN)
    ));
    MembershipEntity workerMembership = membershipRepository.save(new MembershipEntity(
        company,
        workerIdentity,
        Set.of(MembershipRole.WORKER)
    ));

    CompanyEntity companyRef = companyRepository.getReferenceById(company.getId());
    MembershipEntity foremanRef = membershipRepository.getReferenceById(foremanMembership.getId());
    MembershipEntity workerRef = membershipRepository.getReferenceById(workerMembership.getId());

    foremanProfileRepository.save(new ForemanProfileEntity(
        foremanRef,
        companyRef,
        seedProperties.getForemanName(),
        true
    ));

    WorkerProfileEntity workerProfile = new WorkerProfileEntity(
        workerRef,
        companyRef,
        seedProperties.getWorkerName(),
        companyRef.getDefaultLanguage(),
        true
    );
    workerProfileRepository.save(workerProfile);

    CrewEntity crew = crewRepository.save(new CrewEntity(companyRef, seedProperties.getCrewName(), foremanRef));
    workerProfile.setCrew(crew);
    workerProfileRepository.save(workerProfile);

    siteRepository.save(new SiteEntity(
        companyRef,
        seedProperties.getSiteName(),
        seedProperties.getSiteAddress(),
        null,
        true
    ));

    logger.info("Seeded demo company '{}' with admin {}, foreman {}, worker {}",
        seedProperties.getCompanyName(),
        seedProperties.getAdminPhone(),
        seedProperties.getForemanPhone(),
        seedProperties.getWorkerPhone());
  }

  private IdentityEntity findOrCreateIdentity(String phoneE164) {
    return identityRepository.findByPhoneE164(phoneE164)
        .orElseGet(() -> identityRepository.save(new IdentityEntity(phoneE164)));
  }

  private boolean isInMemoryDatabase() {
    String url = dataSourceProperties.getUrl();
    return url != null && url.startsWith("jdbc:h2:mem:");
  }
}
