package com.crewhandshake.features.foreman.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExceptionRepository extends JpaRepository<ExceptionEntity, UUID> {
  List<ExceptionEntity> findByCompanyIdAndCrewIdAndStatus(UUID companyId, UUID crewId, ExceptionStatus status);
  Optional<ExceptionEntity> findByCompanyIdAndId(UUID companyId, UUID id);
  Optional<ExceptionEntity> findByCompanyIdAndTimeEntryIdAndType(UUID companyId, UUID timeEntryId, ExceptionType type);
  Optional<ExceptionEntity> findByCompanyIdAndReviewRequestIdAndType(UUID companyId, UUID reviewRequestId, ExceptionType type);

  long countByCompanyIdAndStatus(UUID companyId, ExceptionStatus status);
}
