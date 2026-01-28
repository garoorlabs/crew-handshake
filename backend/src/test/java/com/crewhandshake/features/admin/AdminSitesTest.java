package com.crewhandshake.features.admin;

import com.crewhandshake.common.security.AuthSession;
import com.crewhandshake.common.security.MembershipRole;
import com.crewhandshake.common.security.SessionService;
import com.crewhandshake.features.admin.persistence.SiteEntity;
import com.crewhandshake.features.admin.persistence.SiteRepository;
import com.crewhandshake.features.auth.persistence.CompanyEntity;
import com.crewhandshake.features.auth.persistence.CompanyRepository;
import com.crewhandshake.features.auth.persistence.IdentityEntity;
import com.crewhandshake.features.auth.persistence.IdentityRepository;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import com.crewhandshake.features.auth.persistence.MembershipRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSitesTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private IdentityRepository identityRepository;

  @Autowired
  private MembershipRepository membershipRepository;

  @Autowired
  private SiteRepository siteRepository;

  @Test
  void createSiteRequiresName() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Acme Sites"));
    MembershipEntity admin = createMembership(company, Set.of(MembershipRole.ADMIN), "+14155550001");
    MockHttpSession session = sessionFor(company, admin);

    mockMvc.perform(post("/api/v1/admin/sites")
        .session(session)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"address\":\"123 Main\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.fieldErrors.name").exists());
  }

  @Test
  void createSiteAppearsInAdminAndForemanLists() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Site Roster Co"));
    MembershipEntity admin = createMembership(company, Set.of(MembershipRole.ADMIN), "+14155550002");
    MembershipEntity foreman = createMembership(company, Set.of(MembershipRole.FOREMAN), "+14155550003");
    MockHttpSession adminSession = sessionFor(company, admin);
    MockHttpSession foremanSession = sessionFor(company, foreman);

    mockMvc.perform(post("/api/v1/admin/sites")
        .session(adminSession)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"name\":\"Central Yard\",\"address\":\"123 Main\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/v1/admin/sites")
        .session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name").value("Central Yard"));

    mockMvc.perform(get("/api/v1/foreman/sites")
        .session(foremanSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name").value("Central Yard"));
  }

  @Test
  void inactiveSitesAreHiddenFromForemanList() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Inactive Sites Co"));
    MembershipEntity admin = createMembership(company, Set.of(MembershipRole.ADMIN), "+14155550004");
    MembershipEntity foreman = createMembership(company, Set.of(MembershipRole.FOREMAN), "+14155550005");
    MockHttpSession adminSession = sessionFor(company, admin);
    MockHttpSession foremanSession = sessionFor(company, foreman);

    mockMvc.perform(post("/api/v1/admin/sites")
        .session(adminSession)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"name\":\"North Yard\",\"address\":\"456 Oak\"}"))
        .andExpect(status().isOk());

    List<SiteEntity> sites = siteRepository.findByCompanyId(company.getId());
    SiteEntity site = sites.get(0);

    mockMvc.perform(put("/api/v1/admin/sites")
        .session(adminSession)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"siteId\":\"" + site.getId() + "\",\"name\":\"North Yard\",\"address\":\"456 Oak\",\"notes\":\"\",\"active\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));

    mockMvc.perform(get("/api/v1/foreman/sites")
        .session(foremanSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void foremanCannotAccessAdminSites() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Foreman Blocked Co"));
    MembershipEntity foreman = createMembership(company, Set.of(MembershipRole.FOREMAN), "+14155550006");
    MockHttpSession session = sessionFor(company, foreman);

    mockMvc.perform(get("/api/v1/admin/sites")
        .session(session))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
  }

  @Test
  void listSitesIsScopedToActiveCompany() throws Exception {
    CompanyEntity primary = companyRepository.save(new CompanyEntity("Primary Co"));
    CompanyEntity secondary = companyRepository.save(new CompanyEntity("Secondary Co"));
    MembershipEntity admin = createMembership(primary, Set.of(MembershipRole.ADMIN), "+14155550007");
    MockHttpSession session = sessionFor(primary, admin);

    siteRepository.save(new SiteEntity(primary, "Primary Yard", null, null, true));
    siteRepository.save(new SiteEntity(secondary, "Secondary Yard", null, null, true));

    mockMvc.perform(get("/api/v1/admin/sites")
        .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name").value("Primary Yard"));
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
