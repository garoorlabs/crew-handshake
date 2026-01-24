package com.crewhandshake.features.admin.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForemanProfileRepository extends JpaRepository<ForemanProfileEntity, UUID> {
  List<ForemanProfileEntity> findByCompanyId(UUID companyId);
  Optional<ForemanProfileEntity> findByCompanyIdAndMembershipId(UUID companyId, UUID membershipId);
}
