package com.crewhandshake.features.admin.persistence;

import com.crewhandshake.features.auth.persistence.CompanyEntity;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "foreman_profiles")
public class ForemanProfileEntity {
  @Id
  @Column(name = "membership_id", columnDefinition = "uuid")
  private UUID membershipId;

  @OneToOne(optional = false)
  @MapsId
  @JoinColumn(name = "membership_id", nullable = false)
  private MembershipEntity membership;

  @ManyToOne(optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private CompanyEntity company;

  @Column(name = "display_name", nullable = false, length = 200)
  private String displayName;

  @Column(name = "active", nullable = false)
  private boolean active;

  protected ForemanProfileEntity() {}

  public ForemanProfileEntity(MembershipEntity membership, CompanyEntity company, String displayName, boolean active) {
    this.membership = membership;
    this.company = company;
    this.displayName = displayName;
    this.active = active;
  }

  public UUID getMembershipId() {
    return membershipId;
  }

  public MembershipEntity getMembership() {
    return membership;
  }

  public CompanyEntity getCompany() {
    return company;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
