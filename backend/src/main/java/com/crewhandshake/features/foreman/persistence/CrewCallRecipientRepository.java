package com.crewhandshake.features.foreman.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewCallRecipientRepository extends JpaRepository<CrewCallRecipientEntity, UUID> {
  List<CrewCallRecipientEntity> findByCompanyIdAndCrewCallId(UUID companyId, UUID crewCallId);
  Optional<CrewCallRecipientEntity> findByCompanyIdAndId(UUID companyId, UUID id);
  Optional<CrewCallRecipientEntity> findByTokenHash(String tokenHash);
  Optional<CrewCallRecipientEntity> findByCompanyIdAndCrewCallIdAndWorkerMembershipId(UUID companyId, UUID crewCallId, UUID workerMembershipId);
  List<CrewCallRecipientEntity> findByCompanyIdAndAvailabilityAfterIsNotNullAndStandbyClosedAtIsNullAndCrewCall_WorkDate(UUID companyId, LocalDate workDate);
}
