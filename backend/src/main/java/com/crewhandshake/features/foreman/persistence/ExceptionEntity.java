package com.crewhandshake.features.foreman.persistence;

import com.crewhandshake.features.admin.persistence.CrewEntity;
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
import java.util.UUID;

@Entity
@Table(name = "exceptions")
public class ExceptionEntity {
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
  @JoinColumn(name = "worker_membership_id", nullable = false)
  private MembershipEntity workerMembership;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 32)
  private ExceptionType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private ExceptionStatus status;

  @ManyToOne
  @JoinColumn(name = "time_entry_id")
  private TimeEntryEntity timeEntry;

  @ManyToOne
  @JoinColumn(name = "review_request_id")
  private ReviewRequestEntity reviewRequest;

  @Enumerated(EnumType.STRING)
  @Column(name = "resolution_action", length = 32)
  private ExceptionResolutionAction resolutionAction;

  @Column(name = "resolution_reason", length = 200)
  private String resolutionReason;

  @Column(name = "resolution_note", length = 400)
  private String resolutionNote;

  @ManyToOne
  @JoinColumn(name = "resolved_by_membership_id")
  private MembershipEntity resolvedByMembership;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected ExceptionEntity() {}

  public ExceptionEntity(CompanyEntity company,
                         CrewEntity crew,
                         MembershipEntity workerMembership,
                         ExceptionType type,
                         ExceptionStatus status,
                         TimeEntryEntity timeEntry,
                         ReviewRequestEntity reviewRequest,
                         Instant createdAt) {
    this.company = company;
    this.crew = crew;
    this.workerMembership = workerMembership;
    this.type = type;
    this.status = status;
    this.timeEntry = timeEntry;
    this.reviewRequest = reviewRequest;
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

  public MembershipEntity getWorkerMembership() {
    return workerMembership;
  }

  public ExceptionType getType() {
    return type;
  }

  public ExceptionStatus getStatus() {
    return status;
  }

  public void setStatus(ExceptionStatus status) {
    this.status = status;
  }

  public TimeEntryEntity getTimeEntry() {
    return timeEntry;
  }

  public ReviewRequestEntity getReviewRequest() {
    return reviewRequest;
  }

  public ExceptionResolutionAction getResolutionAction() {
    return resolutionAction;
  }

  public String getResolutionReason() {
    return resolutionReason;
  }

  public String getResolutionNote() {
    return resolutionNote;
  }

  public MembershipEntity getResolvedByMembership() {
    return resolvedByMembership;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
  }

  public void resolve(ExceptionResolutionAction action, String reason, String note, MembershipEntity resolvedBy, Instant resolvedAt) {
    this.resolutionAction = action;
    this.resolutionReason = reason;
    this.resolutionNote = note;
    this.resolvedByMembership = resolvedBy;
    this.resolvedAt = resolvedAt;
    this.status = ExceptionStatus.RESOLVED;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
