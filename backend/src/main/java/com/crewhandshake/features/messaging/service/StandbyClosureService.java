package com.crewhandshake.features.messaging.service;

import com.crewhandshake.features.auth.persistence.CompanyEntity;
import com.crewhandshake.features.auth.persistence.CompanyRepository;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientEntity;
import com.crewhandshake.features.foreman.persistence.CrewCallRecipientRepository;
import com.crewhandshake.features.foreman.persistence.CrewCallStatus;
import com.crewhandshake.features.foreman.persistence.HandshakeStatus;
import com.crewhandshake.features.foreman.persistence.RecipientSendStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StandbyClosureService {
  private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");
  private static final Logger logger = LoggerFactory.getLogger(StandbyClosureService.class);

  private final CompanyRepository companyRepository;
  private final CrewCallRecipientRepository crewCallRecipientRepository;
  private final SmsProvider smsProvider;

  public StandbyClosureService(CompanyRepository companyRepository,
                               CrewCallRecipientRepository crewCallRecipientRepository,
                               SmsProvider smsProvider) {
    this.companyRepository = companyRepository;
    this.crewCallRecipientRepository = crewCallRecipientRepository;
    this.smsProvider = smsProvider;
  }

  @Scheduled(cron = "0 */15 * * * *")
  @Transactional
  public void runStandbyClosureJob() {
    processStandbyClosures(Instant.now());
  }

  @Transactional
  void processStandbyClosures(Instant now) {
    LocalDate workDate = LocalDate.ofInstant(now, DEFAULT_ZONE);
    LocalTime currentTime = LocalDateTime.ofInstant(now, DEFAULT_ZONE).toLocalTime();

    for (CompanyEntity company : companyRepository.findAll()) {
      if (currentTime.isBefore(company.getStandbyCutoffTime())) {
        continue;
      }
      List<CrewCallRecipientEntity> recipients = crewCallRecipientRepository
          .findByCompanyIdAndAvailabilityAfterIsNotNullAndStandbyClosedAtIsNullAndCrewCall_WorkDate(company.getId(), workDate);

      for (CrewCallRecipientEntity recipient : recipients) {
        if (!isEligible(recipient)) {
          continue;
        }
        RecipientSendStatus sendStatus = RecipientSendStatus.SENT;
        String sendError = null;
        try {
          smsProvider.sendStandbyClosure(recipient.getWorkerMembership().getIdentity().getPhoneE164(), buildMessage(company));
        } catch (Exception ex) {
          sendStatus = RecipientSendStatus.FAILED;
          sendError = "SMS failed";
          logger.warn("Standby closure SMS failed for recipient {}", recipient.getId(), ex);
        }
        recipient.setStandbyClosedAt(now);
        recipient.setStandbySendStatus(sendStatus);
        recipient.setStandbySendError(sendError);
        crewCallRecipientRepository.save(recipient);
      }
    }
  }

  private boolean isEligible(CrewCallRecipientEntity recipient) {
    if (recipient.getCrewCall().getStatus() == CrewCallStatus.CANCELLED) {
      return false;
    }
    HandshakeStatus status = recipient.getHandshakeStatus();
    if (status != HandshakeStatus.CANT && status != HandshakeStatus.NEED_CHANGE) {
      return false;
    }
    if (recipient.getOverrideSite() != null || recipient.getOverrideStartAt() != null || recipient.getOverrideMeetPoint() != null) {
      return false;
    }
    return true;
  }

  private String buildMessage(CompanyEntity company) {
    return company.getName() + ": No assignment today. We'll reach out if something changes.";
  }
}
