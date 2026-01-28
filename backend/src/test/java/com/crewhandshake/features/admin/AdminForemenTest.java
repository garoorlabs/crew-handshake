package com.crewhandshake.features.admin;

import com.crewhandshake.common.security.AuthSession;
import com.crewhandshake.common.security.MembershipRole;
import com.crewhandshake.common.security.SessionService;
import com.crewhandshake.features.admin.persistence.ForemanProfileEntity;
import com.crewhandshake.features.admin.persistence.ForemanProfileRepository;
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
class AdminForemenTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private IdentityRepository identityRepository;

  @Autowired
  private MembershipRepository membershipRepository;

  @Autowired
  private ForemanProfileRepository foremanProfileRepository;

  @Test
  void createForemanRequiresPhone() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Foremen Co"));
    MembershipEntity admin = createMembership(company, Set.of(MembershipRole.ADMIN), "+14155560001");
    MockHttpSession session = sessionFor(company, admin);

    mockMvc.perform(post("/api/v1/admin/foremen")
        .session(session)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"displayName\":\"Lead\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.fieldErrors.phone").exists());
  }

  @Test
  void createAndUpdateForeman() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Roster Co"));
    MembershipEntity admin = createMembership(company, Set.of(MembershipRole.ADMIN), "+14155560002");
    MockHttpSession session = sessionFor(company, admin);

    mockMvc.perform(post("/api/v1/admin/foremen")
        .session(session)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"displayName\":\"Lead\",\"phone\":\"+14155561001\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("Lead"));

    List<ForemanProfileEntity> foremen = foremanProfileRepository.findByCompanyId(company.getId());
    ForemanProfileEntity foreman = foremen.get(0);

    mockMvc.perform(put("/api/v1/admin/foremen")
        .session(session)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"membershipId\":\"" + foreman.getMembershipId() + "\",\"displayName\":\"Lead\",\"active\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));

    mockMvc.perform(get("/api/v1/admin/foremen")
        .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].active").value(false));
  }

  @Test
  void foremanCannotAccessAdminForemen() throws Exception {
    CompanyEntity company = companyRepository.save(new CompanyEntity("Foreman Blocked"));
    MembershipEntity foreman = createMembership(company, Set.of(MembershipRole.FOREMAN), "+14155560003");
    MockHttpSession session = sessionFor(company, foreman);

    mockMvc.perform(get("/api/v1/admin/foremen")
        .session(session))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
  }

  @Test
  void listForemenIsScopedToActiveCompany() throws Exception {
    CompanyEntity primary = companyRepository.save(new CompanyEntity("Primary Co"));
    CompanyEntity secondary = companyRepository.save(new CompanyEntity("Secondary Co"));
    MembershipEntity primaryAdmin = createMembership(primary, Set.of(MembershipRole.ADMIN), "+14155560004");
    MembershipEntity secondaryAdmin = createMembership(secondary, Set.of(MembershipRole.ADMIN), "+14155560005");
    MockHttpSession primarySession = sessionFor(primary, primaryAdmin);
    MockHttpSession secondarySession = sessionFor(secondary, secondaryAdmin);

    mockMvc.perform(post("/api/v1/admin/foremen")
        .session(primarySession)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"displayName\":\"Primary Lead\",\"phone\":\"+14155560006\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/admin/foremen")
        .session(secondarySession)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"displayName\":\"Secondary Lead\",\"phone\":\"+14155560007\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/v1/admin/foremen")
        .session(primarySession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].displayName").value("Primary Lead"));
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
