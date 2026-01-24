package com.crewhandshake.features.foreman.persistence;

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
@Table(name = "review_requests")
public class ReviewRequestEntity {
  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private CompanyEntity company;

  @ManyToOne(optional = false)
  @JoinColumn(name = "worker_membership_id", nullable = false)
  private MembershipEntity workerMembership;

  @Column(name = "work_date", nullable = false)
  private LocalDate workDate;

  @Column(name = "reason", nullable = false, length = 120)
  private String reason;

  @Column(name = "note", length = 400)
  private String note;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private ReviewRequestStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected ReviewRequestEntity() {}

  public ReviewRequestEntity(CompanyEntity company,
                             MembershipEntity workerMembership,
                             LocalDate workDate,
                             String reason,
                             String note,
                             ReviewRequestStatus status,
                             Instant createdAt) {
    this.company = company;
    this.workerMembership = workerMembership;
    this.workDate = workDate;
    this.reason = reason;
    this.note = note;
    this.status = status;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public CompanyEntity getCompany() {
    return company;
  }

  public MembershipEntity getWorkerMembership() {
    return workerMembership;
  }

  public LocalDate getWorkDate() {
    return workDate;
  }

  public String getReason() {
    return reason;
  }

  public String getNote() {
    return note;
  }

  public ReviewRequestStatus getStatus() {
    return status;
  }

  public void setStatus(ReviewRequestStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
