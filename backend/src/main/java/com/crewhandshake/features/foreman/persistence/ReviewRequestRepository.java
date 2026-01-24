package com.crewhandshake.features.foreman.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRequestRepository extends JpaRepository<ReviewRequestEntity, UUID> {
  List<ReviewRequestEntity> findByCompanyIdAndWorkDateAndStatus(UUID companyId, LocalDate workDate, ReviewRequestStatus status);
  Optional<ReviewRequestEntity> findByCompanyIdAndId(UUID companyId, UUID id);
  Optional<ReviewRequestEntity> findByCompanyIdAndWorkerMembershipIdAndWorkDate(UUID companyId, UUID workerMembershipId, LocalDate workDate);
}
