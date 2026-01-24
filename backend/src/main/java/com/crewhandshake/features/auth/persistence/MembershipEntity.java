package com.crewhandshake.features.auth.persistence;

import com.crewhandshake.common.security.MembershipRole;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "memberships")
public class MembershipEntity {
  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private CompanyEntity company;

  @ManyToOne(optional = false)
  @JoinColumn(name = "identity_id", nullable = false)
  private IdentityEntity identity;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "membership_roles", joinColumns = @JoinColumn(name = "membership_id"))
  @Column(name = "role", nullable = false, length = 32)
  @Enumerated(EnumType.STRING)
  private Set<MembershipRole> roles = new HashSet<>();

  protected MembershipEntity() {}

  public MembershipEntity(CompanyEntity company, IdentityEntity identity, Set<MembershipRole> roles) {
    this.company = company;
    this.identity = identity;
    this.roles = roles == null ? new HashSet<>() : new HashSet<>(roles);
  }

  public UUID getId() {
    return id;
  }

  public CompanyEntity getCompany() {
    return company;
  }

  public IdentityEntity getIdentity() {
    return identity;
  }

  public Set<MembershipRole> getRoles() {
    return roles;
  }
}
