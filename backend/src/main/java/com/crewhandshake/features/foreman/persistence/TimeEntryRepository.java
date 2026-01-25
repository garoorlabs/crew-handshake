package com.crewhandshake.features.foreman.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeEntryRepository extends JpaRepository<TimeEntryEntity, UUID> {
  Optional<TimeEntryEntity> findByCompanyIdAndWorkerMembershipIdAndCrewCallIdAndWorkDate(
      UUID companyId, UUID workerMembershipId, UUID crewCallId, LocalDate workDate);

  List<TimeEntryEntity> findByCompanyIdAndCrewCallId(UUID companyId, UUID crewCallId);

  List<TimeEntryEntity> findByCompanyIdAndWorkerMembershipIdAndWorkDateBetween(
      UUID companyId, UUID workerMembershipId, LocalDate start, LocalDate end);

  List<TimeEntryEntity> findByCompanyIdAndWorkDateAndCrewCallId(UUID companyId, LocalDate workDate, UUID crewCallId);

  Optional<TimeEntryEntity> findByCompanyIdAndWorkerMembershipIdAndWorkDate(UUID companyId, UUID workerMembershipId, LocalDate workDate);

  Optional<TimeEntryEntity> findByCompanyIdAndId(UUID companyId, UUID id);

  long countByCompanyIdAndWorkDateBetween(UUID companyId, LocalDate start, LocalDate end);

  List<TimeEntryEntity> findByCompanyIdAndWorkDateBetween(UUID companyId, LocalDate start, LocalDate end);
}
