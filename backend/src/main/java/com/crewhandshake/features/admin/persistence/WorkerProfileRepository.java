package com.crewhandshake.features.admin.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerProfileRepository extends JpaRepository<WorkerProfileEntity, UUID> {
  List<WorkerProfileEntity> findByCompanyId(UUID companyId);
  List<WorkerProfileEntity> findByCompanyIdAndCrewId(UUID companyId, UUID crewId);
  Optional<WorkerProfileEntity> findByCompanyIdAndMembershipId(UUID companyId, UUID membershipId);
}
