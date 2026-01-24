package com.crewhandshake.features.messaging.service;

import com.crewhandshake.common.security.MembershipRole;
import com.crewhandshake.features.admin.persistence.CrewEntity;
import com.crewhandshake.features.admin.persistence.SiteEntity;
import com.crewhandshake.features.auth.persistence.CompanyEntity;
import com.crewhandshake.features.auth.persistence.CompanyRepository;
import com.crewhandshake.features.auth.persistence.IdentityEntity;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import com.crewhandshake.features.foreman.persistence.AvailabilityAfter;
import com.crewhandshake.features.foreman.persistence.CrewCallEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientRepository;
import com.crewhandshake.features.foreman.persistence.CrewCallStatus;
import com.crewhandshake.features.foreman.persistence.HandshakeStatus;
import com.crewhandshake.features.foreman.persistence.RecipientSendStatus;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandbyClosureServiceTest {
  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private CrewCallRecipientRepository crewCallRecipientRepository;

  @Mock
  private SmsProvider smsProvider;

  private StandbyClosureService standbyClosureService;

  @BeforeEach
  void setUp() {
    standbyClosureService = new StandbyClosureService(companyRepository, crewCallRecipientRepository, smsProvider);
  }

  @Test
  void sendsStandbyClosureAfterCutoff() {
    CompanyEntity company = new CompanyEntity("Acme Construction");
    company.setStandbyCutoffTime(LocalTime.of(18, 0));
    setId(company, UUID.randomUUID());

    LocalDate workDate = LocalDate.of(2026, 1, 23);
    Instant now = workDate.atTime(19, 0).toInstant(ZoneOffset.UTC);

    CrewCallRecipientEntity recipient = buildRecipient(company, workDate);

    when(companyRepository.findAll()).thenReturn(List.of(company));
    when(crewCallRecipientRepository
        .findByCompanyIdAndAvailabilityAfterIsNotNullAndStandbyClosedAtIsNullAndCrewCall_WorkDate(company.getId(), workDate))
        .thenReturn(List.of(recipient));

    standbyClosureService.processStandbyClosures(now);

    verify(smsProvider).sendStandbyClosure(eq("+14155550000"), anyString());

    ArgumentCaptor<CrewCallRecipientEntity> captor = ArgumentCaptor.forClass(CrewCallRecipientEntity.class);
    verify(crewCallRecipientRepository).save(captor.capture());
    CrewCallRecipientEntity saved = captor.getValue();
    assertThat(saved.getStandbyClosedAt()).isEqualTo(now);
    assertThat(saved.getStandbySendStatus()).isEqualTo(RecipientSendStatus.SENT);
    assertThat(saved.getStandbySendError()).isNull();
  }

  @Test
  void skipsBeforeCutoff() {
    CompanyEntity company = new CompanyEntity("Acme Construction");
    company.setStandbyCutoffTime(LocalTime.of(18, 0));
    setId(company, UUID.randomUUID());

    Instant now = LocalDate.of(2026, 1, 23).atTime(9, 0).toInstant(ZoneOffset.UTC);

    when(companyRepository.findAll()).thenReturn(List.of(company));

    standbyClosureService.processStandbyClosures(now);

    verifyNoInteractions(crewCallRecipientRepository);
    verifyNoInteractions(smsProvider);
  }

  private CrewCallRecipientEntity buildRecipient(CompanyEntity company, LocalDate workDate) {
    IdentityEntity identity = new IdentityEntity("+14155550000");
    MembershipEntity membership = new MembershipEntity(company, identity, Set.of(MembershipRole.WORKER));
    CrewEntity crew = new CrewEntity(company, "Crew A", membership);
    SiteEntity site = new SiteEntity(company, "Site A", "123 Main", null, true);
    Instant startAt = workDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    CrewCallEntity crewCall = new CrewCallEntity(company, crew, site, startAt, workDate, "Gate A", membership, CrewCallStatus.ACTIVE, startAt);
    CrewCallRecipientEntity recipient = new CrewCallRecipientEntity(
        company,
        crewCall,
        membership,
        "hash",
        startAt.plusSeconds(3600),
        RecipientSendStatus.SENT,
        null,
        startAt
    );
    recipient.setHandshakeStatus(HandshakeStatus.CANT);
    recipient.setAvailabilityAfter(AvailabilityAfter.AFTER_9);
    return recipient;
  }

  private static void setId(Object entity, UUID id) {
    try {
      Field field = entity.getClass().getDeclaredField("id");
      field.setAccessible(true);
      field.set(entity, id);
    } catch (NoSuchFieldException | IllegalAccessException ex) {
      throw new IllegalStateException("Unable to set id field", ex);
    }
  }
}
