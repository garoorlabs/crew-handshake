package com.crewhandshake.features.auth.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentityRepository extends JpaRepository<IdentityEntity, UUID> {
  Optional<IdentityEntity> findByPhoneE164(String phoneE164);
}
