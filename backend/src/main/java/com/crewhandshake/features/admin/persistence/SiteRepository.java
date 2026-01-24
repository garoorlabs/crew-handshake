package com.crewhandshake.features.admin.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<SiteEntity, UUID> {
  List<SiteEntity> findByCompanyId(UUID companyId);
  Optional<SiteEntity> findByCompanyIdAndId(UUID companyId, UUID id);
}
