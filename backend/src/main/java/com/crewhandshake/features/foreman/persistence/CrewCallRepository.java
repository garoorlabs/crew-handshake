package com.crewhandshake.features.foreman.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewCallRepository extends JpaRepository<CrewCallEntity, UUID> {
  Optional<CrewCallEntity> findTopByCompanyIdAndCrewIdAndWorkDateOrderByCreatedAtDesc(UUID companyId, UUID crewId, LocalDate workDate);
  List<CrewCallEntity> findByCompanyIdAndCrewIdAndWorkDate(UUID companyId, UUID crewId, LocalDate workDate);
  Optional<CrewCallEntity> findByCompanyIdAndId(UUID companyId, UUID id);
}
