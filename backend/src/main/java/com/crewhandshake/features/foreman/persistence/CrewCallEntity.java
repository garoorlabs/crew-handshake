package com.crewhandshake.features.foreman.persistence;

import com.crewhandshake.features.admin.persistence.CrewEntity;
import com.crewhandshake.features.admin.persistence.SiteEntity;
import com.crewhandshake.features.auth.persistence.CompanyEntity;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "crew_calls")
public class CrewCallEntity {
  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private CompanyEntity company;

  @ManyToOne(optional = false)
  @JoinColumn(name = "crew_id", nullable = false)
  private CrewEntity crew;

  @ManyToOne(optional = false)
  @JoinColumn(name = "site_id", nullable = false)
  private SiteEntity site;

  @Column(name = "start_at", nullable = false)
  private Instant startAt;

  @Column(name = "work_date", nullable = false)
  private LocalDate workDate;

  @Column(name = "meet_point", nullable = false, length = 200)
  private String meetPoint;

  @ManyToOne(optional = false)
  @JoinColumn(name = "sent_by_membership_id", nullable = false)
  private MembershipEntity sentByMembership;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private CrewCallStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected CrewCallEntity() {}

  public CrewCallEntity(CompanyEntity company,
                        CrewEntity crew,
                        SiteEntity site,
                        Instant startAt,
                        LocalDate workDate,
                        String meetPoint,
                        MembershipEntity sentByMembership,
                        CrewCallStatus status,
                        Instant createdAt) {
    this.company = company;
    this.crew = crew;
    this.site = site;
    this.startAt = startAt;
    this.workDate = workDate;
    this.meetPoint = meetPoint;
    this.sentByMembership = sentByMembership;
    this.status = status;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public CompanyEntity getCompany() {
    return company;
  }

  public CrewEntity getCrew() {
    return crew;
  }

  public SiteEntity getSite() {
    return site;
  }

  public void setSite(SiteEntity site) {
    this.site = site;
  }

  public Instant getStartAt() {
    return startAt;
  }

  public void setStartAt(Instant startAt) {
    this.startAt = startAt;
  }

  public LocalDate getWorkDate() {
    return workDate;
  }

  public String getMeetPoint() {
    return meetPoint;
  }

  public void setMeetPoint(String meetPoint) {
    this.meetPoint = meetPoint;
  }

  public MembershipEntity getSentByMembership() {
    return sentByMembership;
  }

  public CrewCallStatus getStatus() {
    return status;
  }

  public void setStatus(CrewCallStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
