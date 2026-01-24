package com.crewhandshake.features.auth;

import com.crewhandshake.common.security.MembershipRole;
import com.crewhandshake.features.auth.persistence.CompanyEntity;
import com.crewhandshake.features.auth.persistence.CompanyRepository;
import com.crewhandshake.features.auth.persistence.IdentityEntity;
import com.crewhandshake.features.auth.persistence.IdentityRepository;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import com.crewhandshake.features.auth.persistence.MembershipRepository;
import com.crewhandshake.features.messaging.service.SmsProvider;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private IdentityRepository identityRepository;

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private MembershipRepository membershipRepository;

  @Autowired
  private TestSmsProvider testSmsProvider;

  @Test
  void otpFlowCreatesSessionAndLoadsMe() throws Exception {
    String phone = "+14155551212";

    mockMvc.perform(post("/api/v1/auth/otp/start")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"phone\": \"" + phone + "\"}"))
        .andExpect(status().isOk());

    IdentityEntity identity = identityRepository.findByPhoneE164(phone).orElseThrow();
    CompanyEntity company = companyRepository.save(new CompanyEntity("Acme Construction"));
    membershipRepository.save(new MembershipEntity(company, identity, Set.of(MembershipRole.ADMIN)));

    String code = testSmsProvider.getLastCode();
    assertThat(code).isNotBlank();

    MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/otp/verify")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"phone\": \"" + phone + "\", \"code\": \"" + code + "\"}"))
        .andExpect(status().isOk())
        .andReturn();

    MockHttpSession session = (MockHttpSession) verifyResult.getRequest().getSession(false);
    assertThat(session).isNotNull();

    mockMvc.perform(get("/api/v1/me")
        .session(session))
        .andExpect(status().isOk());
  }

  @TestConfiguration
  static class TestMessagingConfig {
    @Bean
    @Primary
    TestSmsProvider testSmsProvider() {
      return new TestSmsProvider();
    }
  }

  static class TestSmsProvider implements SmsProvider {
    private String lastCode;

    @Override
    public void sendOtp(String phoneE164, String code) {
      this.lastCode = code;
    }

    String getLastCode() {
      return lastCode;
    }
  }
}
