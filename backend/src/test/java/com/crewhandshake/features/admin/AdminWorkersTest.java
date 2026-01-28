package com.crewhandshake.features.admin;

import com.crewhandshake.common.security.AuthSession;
import com.crewhandshake.common.security.MembershipRole;
import com.crewhandshake.common.security.SessionService;
import com.crewhandshake.features.admin.persistence.CrewEntity;
import com.crewhandshake.features.admin.persistence.CrewRepository;
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
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminWorkersTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private IdentityRepository identityRepository;

  @Autowired
  private MembershipRepository membershipRepository;

  @Autowired
  private WorkerProfileRepository workerProfileRepository;

  @Autowired
  private CrewRepository crewRepository;

  @Autowired
  private SiteRepository siteRepository;

  @Test
  void createWorkerRejectsInvalidPhone() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Workers Co"));
    MembershipEntity admin = createMembership(company, Set.of(MembershipRole.ADMIN), "+14155570001");
    MockHttpSession session = sessionFor(company, admin);

    mockMvc.perform(post("/api/v1/admin/workers")
        .session(session)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"displayName\":\"Worker\",\"phone\":\"123\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.fieldErrors.phone").exists());
  }

  @Test
  void createWorkerCreatesIdentityMembershipAndProfile() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Roster Co"));
    MembershipEntity admin = createMembership(company, Set.of(MembershipRole.ADMIN), "+14155570002");
    MockHttpSession session = sessionFor(company, admin);

    mockMvc.perform(post("/api/v1/admin/workers")
        .session(session)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"displayName\":\"Worker One\",\"phone\":\"+14155570003\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("Worker One"));

    IdentityEntity identity = identityRepository.findByPhoneE164("+14155570003").orElseThrow();
    MembershipEntity membership = membershipRepository.findByIdentityIdAndCompanyId(identity.getId(), company.getId())
        .orElseThrow();
    WorkerProfileEntity worker = workerProfileRepository.findByCompanyIdAndMembershipId(company.getId(), membership.getId())
        .orElseThrow();
    assertTrue(membership.getRoles().contains(MembershipRole.WORKER));
    assertTrue(worker.getCompany().getId().equals(company.getId()));
  }

  @Test
  void duplicateWorkerMembershipReturnsConflict() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Duplicate Co"));
    MembershipEntity admin = createMembership(company, Set.of(MembershipRole.ADMIN), "+14155570004");
    MockHttpSession session = sessionFor(company, admin);

    mockMvc.perform(post("/api/v1/admin/workers")
        .session(session)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"displayName\":\"Worker Two\",\"phone\":\"+14155570005\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/admin/workers")
        .session(session)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"displayName\":\"Worker Two\",\"phone\":\"+14155570005\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errorCode").value("CONFLICT"));
  }

  @Test
  void inactiveWorkerAssignmentIsRejected() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Inactive Co"));
    MembershipEntity admin = createMembership(company, Set.of(MembershipRole.ADMIN), "+14155570006");
    MembershipEntity foreman = createMembership(company, Set.of(MembershipRole.FOREMAN), "+14155570007");
    CrewEntity crew = crewRepository.save(new CrewEntity(company, "Crew A", foreman));
    MockHttpSession session = sessionFor(company, admin);

    mockMvc.perform(post("/api/v1/admin/workers")
        .session(session)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"displayName\":\"Worker Three\",\"phone\":\"+14155570008\",\"crewId\":\"" + crew.getId() + "\",\"active\":false}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.fieldErrors.crewId").exists());
  }

  @Test
  void foremanCannotAccessAdminWorkers() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Foreman Blocked"));
    MembershipEntity foreman = createMembership(company, Set.of(MembershipRole.FOREMAN), "+14155570009");
    MockHttpSession session = sessionFor(company, foreman);

    mockMvc.perform(get("/api/v1/admin/workers")
        .session(session))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
  }

  @Test
  void assignedWorkerReceivesCrewCall() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Crew Call Co"));
    MembershipEntity admin = createMembership(company, Set.of(MembershipRole.ADMIN), "+14155570010");
    MembershipEntity foreman = createMembership(company, Set.of(MembershipRole.FOREMAN), "+14155570011");
    CrewEntity crew = crewRepository.save(new CrewEntity(company, "Crew B", foreman));
    SiteEntity site = siteRepository.save(new SiteEntity(company, "Main Site", null, null, true));
    MockHttpSession session = sessionFor(company, admin);

    mockMvc.perform(post("/api/v1/admin/workers")
        .session(session)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"displayName\":\"Worker Four\",\"phone\":\"+14155570012\",\"crewId\":\"" + crew.getId() + "\"}"))
        .andExpect(status().isOk());

    List<WorkerProfileEntity> workers = workerProfileRepository.findByCompanyIdAndCrewId(company.getId(), crew.getId());
    WorkerProfileEntity worker = workers.get(0);

    String startAt = Instant.now().plusSeconds(3600).toString();
    String payload = "{\"crewId\":\"" + crew.getId() + "\",\"siteId\":\"" + site.getId() + "\",\"startAt\":\"" + startAt + "\",\"meetPoint\":\"Gate A\"}";

    mockMvc.perform(post("/api/v1/foreman/crew-calls")
        .session(session)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.recipients", hasSize(1)))
        .andExpect(jsonPath("$.recipients[0].workerMembershipId").value(worker.getMembershipId().toString()));
  }

  private MembershipEntity createMembership(CompanyEntity company, Set<MembershipRole> roles, String phone) {
    IdentityEntity identity = identityRepository.save(new IdentityEntity(phone));
    return membershipRepository.save(new MembershipEntity(company, identity, roles));
  }

  private MockHttpSession sessionFor(CompanyEntity company, MembershipEntity membership) {
    AuthSession session = new AuthSession(
        membership.getIdentity().getId(),
        membership.getIdentity().getPhoneE164(),
        company.getId(),
        membership.getId(),
        membership.getRoles()
    );
    MockHttpSession httpSession = new MockHttpSession();
    httpSession.setAttribute(SessionService.SESSION_KEY, session);
    return httpSession;
  }
}
