package com.crewhandshake.features.worker.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerTimecardLinkRepository extends JpaRepository<WorkerTimecardLinkEntity, UUID> {
  Optional<WorkerTimecardLinkEntity> findByTokenHash(String tokenHash);
  Optional<WorkerTimecardLinkEntity> findByCompanyIdAndWorkerMembershipId(UUID companyId, UUID workerMembershipId);
}
