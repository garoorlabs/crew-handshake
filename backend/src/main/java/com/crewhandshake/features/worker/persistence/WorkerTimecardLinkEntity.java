package com.crewhandshake.features.worker.persistence;

import com.crewhandshake.features.auth.persistence.CompanyEntity;
import com.crewhandshake.features.auth.persistence.MembershipEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "worker_timecard_links")
public class WorkerTimecardLinkEntity {
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

  @Column(name = "token_hash", nullable = false, length = 200)
  private String tokenHash;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected WorkerTimecardLinkEntity() {}

  public WorkerTimecardLinkEntity(CompanyEntity company, MembershipEntity workerMembership, String tokenHash, Instant createdAt) {
    this.company = company;
    this.workerMembership = workerMembership;
    this.tokenHash = tokenHash;
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

  public String getTokenHash() {
    return tokenHash;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
