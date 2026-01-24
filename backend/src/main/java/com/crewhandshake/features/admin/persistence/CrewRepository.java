package com.crewhandshake.features.admin.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewRepository extends JpaRepository<CrewEntity, UUID> {
  List<CrewEntity> findByCompanyId(UUID companyId);
  List<CrewEntity> findByCompanyIdAndForemanMembershipId(UUID companyId, UUID foremanMembershipId);
  Optional<CrewEntity> findByCompanyIdAndId(UUID companyId, UUID id);
}
