package com.crewhandshake.features.admin.persistence;

import com.crewhandshake.features.auth.persistence.CompanyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "sites")
public class SiteEntity {
  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private CompanyEntity company;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 400)
  private String address;

  @Column(length = 500)
  private String notes;

  @Column(nullable = false)
  private boolean active;

  protected SiteEntity() {}

  public SiteEntity(CompanyEntity company, String name, String address, String notes, boolean active) {
    this.company = company;
    this.name = name;
    this.address = address;
    this.notes = notes;
    this.active = active;
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

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
