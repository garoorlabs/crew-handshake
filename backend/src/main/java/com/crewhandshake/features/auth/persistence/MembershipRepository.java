package com.crewhandshake.features.auth.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<MembershipEntity, UUID> {
  List<MembershipEntity> findByIdentityId(UUID identityId);
  Optional<MembershipEntity> findByIdentityIdAndCompanyId(UUID identityId, UUID companyId);
  Optional<MembershipEntity> findByCompanyIdAndId(UUID companyId, UUID id);
  List<MembershipEntity> findByCompanyId(UUID companyId);
}
