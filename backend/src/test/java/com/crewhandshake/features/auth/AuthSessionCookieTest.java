package com.crewhandshake.features.auth;

import com.crewhandshake.common.security.MembershipRole;
import com.crewhandshake.features.auth.persistence.CompanyEntity;
import com.crewhandshake.features.auth.persistence.CompanyRepository;
import com.crewhandshake.features.auth.persistence.IdentityEntity;
import com.crewhandshake.features.auth.persistence.IdentityRepository;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import com.crewhandshake.features.auth.persistence.MembershipRepository;
import com.crewhandshake.features.messaging.service.SmsProvider;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthSessionCookieTest {
  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private IdentityRepository identityRepository;

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private MembershipRepository membershipRepository;

  @Autowired
  private TestSmsProvider testSmsProvider;

  @Test
  void otpVerifyEstablishesSessionCookie() {
    String phone = "+14155551213";

    ResponseEntity<String> startResponse = restTemplate.postForEntity(
        "/api/v1/auth/otp/start",
        Map.of("phone", phone),
        String.class
    );
    assertThat(startResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    IdentityEntity identity = identityRepository.findByPhoneE164(phone).orElseThrow();
    CompanyEntity company = companyRepository.save(new CompanyEntity("Acme Construction"));
    membershipRepository.save(new MembershipEntity(company, identity, Set.of(MembershipRole.ADMIN)));

    String code = testSmsProvider.getLastCode();
    assertThat(code).isNotBlank();

    ResponseEntity<String> verifyResponse = restTemplate.postForEntity(
        "/api/v1/auth/otp/verify",
        Map.of("phone", phone, "code", code),
        String.class
    );
    assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    List<String> setCookies = verifyResponse.getHeaders().get("Set-Cookie");
    assertThat(setCookies).isNotNull();
    assertThat(setCookies).anyMatch(cookie -> cookie.startsWith("JSESSIONID="));
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
