package com.crewhandshake.features.admin.persistence;

import com.crewhandshake.features.auth.persistence.CompanyEntity;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "crews")
public class CrewEntity {
  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private CompanyEntity company;

  @Column(nullable = false, length = 200)
  private String name;

  @ManyToOne(optional = false)
  @JoinColumn(name = "foreman_membership_id", nullable = false)
  private MembershipEntity foremanMembership;

  protected CrewEntity() {}

  public CrewEntity(CompanyEntity company, String name, MembershipEntity foremanMembership) {
    this.company = company;
    this.name = name;
    this.foremanMembership = foremanMembership;
  }

  public UUID getId() {
    return id;
  }

  public CompanyEntity getCompany() {
    return company;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MembershipEntity getForemanMembership() {
    return foremanMembership;
  }

  public void setForemanMembership(MembershipEntity foremanMembership) {
    this.foremanMembership = foremanMembership;
  }
}
