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
@Table(name = "worker_profiles")
public class WorkerProfileEntity {
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

  @ManyToOne
  @JoinColumn(name = "crew_id")
  private CrewEntity crew;

  @Column(name = "display_name", nullable = false, length = 200)
  private String displayName;

  @Column(name = "preferred_language", nullable = false, length = 8)
  private String preferredLanguage;

  @Column(name = "active", nullable = false)
  private boolean active;

  protected WorkerProfileEntity() {}

  public WorkerProfileEntity(MembershipEntity membership, CompanyEntity company, String displayName, String preferredLanguage, boolean active) {
    this.membership = membership;
    this.company = company;
    this.displayName = displayName;
    this.preferredLanguage = preferredLanguage;
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

  public CrewEntity getCrew() {
    return crew;
  }

  public void setCrew(CrewEntity crew) {
    this.crew = crew;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getPreferredLanguage() {
    return preferredLanguage;
  }

  public void setPreferredLanguage(String preferredLanguage) {
    this.preferredLanguage = preferredLanguage;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
