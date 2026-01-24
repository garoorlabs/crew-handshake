package com.crewhandshake.features.auth.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRequestAttemptRepository extends JpaRepository<OtpRequestAttemptEntity, UUID> {
  int countByPhoneE164AndCreatedAtAfter(String phoneE164, Instant since);

  int countByIpAddressAndCreatedAtAfter(String ipAddress, Instant since);

  int deleteByCreatedAtBefore(Instant cutoff);
}
