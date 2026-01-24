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
@Table(name = "time_entries")
public class TimeEntryEntity {
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

  @ManyToOne(optional = false)
  @JoinColumn(name = "crew_call_id", nullable = false)
  private CrewCallEntity crewCall;

  @Column(name = "work_date", nullable = false)
  private LocalDate workDate;

  @Column(name = "check_in_at")
  private Instant checkInAt;

  @Column(name = "check_out_at")
  private Instant checkOutAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "source", nullable = false, length = 32)
  private TimeEntrySource source;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private TimeEntryStatus status;

  @Column(name = "edited", nullable = false)
  private boolean edited;

  @Column(name = "edit_reason", length = 200)
  private String editReason;

  @Column(name = "edit_note", length = 400)
  private String editNote;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected TimeEntryEntity() {}

  public TimeEntryEntity(CompanyEntity company,
                         MembershipEntity workerMembership,
                         CrewCallEntity crewCall,
                         LocalDate workDate,
                         TimeEntrySource source,
                         TimeEntryStatus status,
                         Instant createdAt) {
    this.company = company;
    this.workerMembership = workerMembership;
    this.crewCall = crewCall;
    this.workDate = workDate;
    this.source = source;
    this.status = status;
    this.createdAt = createdAt;
    this.edited = false;
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

  public CrewCallEntity getCrewCall() {
    return crewCall;
  }

  public LocalDate getWorkDate() {
    return workDate;
  }

  public Instant getCheckInAt() {
    return checkInAt;
  }

  public void setCheckInAt(Instant checkInAt) {
    this.checkInAt = checkInAt;
  }

  public Instant getCheckOutAt() {
    return checkOutAt;
  }

  public void setCheckOutAt(Instant checkOutAt) {
    this.checkOutAt = checkOutAt;
  }

  public TimeEntrySource getSource() {
    return source;
  }

  public void setSource(TimeEntrySource source) {
    this.source = source;
  }

  public TimeEntryStatus getStatus() {
    return status;
  }

  public void setStatus(TimeEntryStatus status) {
    this.status = status;
  }

  public boolean isEdited() {
    return edited;
  }

  public void markEdited(String reason, String note) {
    this.edited = true;
    this.editReason = reason;
    this.editNote = note;
  }

  public String getEditReason() {
    return editReason;
  }

  public String getEditNote() {
    return editNote;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
