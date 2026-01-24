package com.crewhandshake.features.auth.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpCodeRepository extends JpaRepository<OtpCodeEntity, UUID> {
  Optional<OtpCodeEntity> findTopByIdentityIdOrderByCreatedAtDesc(UUID identityId);
}
