package com.crewhandshake.features.foreman.persistence;

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
import java.util.UUID;

@Entity
@Table(name = "crew_call_recipients")
public class CrewCallRecipientEntity {
  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private CompanyEntity company;

  @ManyToOne(optional = false)
  @JoinColumn(name = "crew_call_id", nullable = false)
  private CrewCallEntity crewCall;

  @ManyToOne(optional = false)
  @JoinColumn(name = "worker_membership_id", nullable = false)
  private MembershipEntity workerMembership;

  @Column(name = "token_hash", nullable = false, length = 200)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "handshake_status", length = 32)
  private HandshakeStatus handshakeStatus;

  @Column(name = "handshake_at")
  private Instant handshakeAt;

  @Column(name = "late_eta_minutes")
  private Integer lateEtaMinutes;

  @Enumerated(EnumType.STRING)
  @Column(name = "availability_after", length = 32)
  private AvailabilityAfter availabilityAfter;

  @Column(name = "availability_different_site_ok")
  private Boolean availabilityDifferentSiteOk;

  @Column(name = "availability_note", length = 400)
  private String availabilityNote;

  @ManyToOne
  @JoinColumn(name = "override_site_id")
  private SiteEntity overrideSite;

  @Column(name = "override_start_at")
  private Instant overrideStartAt;

  @Column(name = "override_meet_point", length = 200)
  private String overrideMeetPoint;

  @Enumerated(EnumType.STRING)
  @Column(name = "send_status", nullable = false, length = 32)
  private RecipientSendStatus sendStatus;

  @Column(name = "send_error", length = 400)
  private String sendError;

  @Column(name = "standby_closed_at")
  private Instant standbyClosedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "standby_send_status", length = 32)
  private RecipientSendStatus standbySendStatus;

  @Column(name = "standby_send_error", length = 400)
  private String standbySendError;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected CrewCallRecipientEntity() {}

  public CrewCallRecipientEntity(CompanyEntity company,
                                 CrewCallEntity crewCall,
                                 MembershipEntity workerMembership,
                                 String tokenHash,
                                 Instant expiresAt,
                                 RecipientSendStatus sendStatus,
                                 String sendError,
                                 Instant createdAt) {
    this.company = company;
    this.crewCall = crewCall;
    this.workerMembership = workerMembership;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.sendStatus = sendStatus;
    this.sendError = sendError;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public CompanyEntity getCompany() {
    return company;
  }

  public CrewCallEntity getCrewCall() {
    return crewCall;
  }

  public MembershipEntity getWorkerMembership() {
    return workerMembership;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public void setTokenHash(String tokenHash) {
    this.tokenHash = tokenHash;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public HandshakeStatus getHandshakeStatus() {
    return handshakeStatus;
  }

  public void setHandshakeStatus(HandshakeStatus handshakeStatus) {
    this.handshakeStatus = handshakeStatus;
  }

  public Instant getHandshakeAt() {
    return handshakeAt;
  }

  public void setHandshakeAt(Instant handshakeAt) {
    this.handshakeAt = handshakeAt;
  }

  public Integer getLateEtaMinutes() {
    return lateEtaMinutes;
  }

  public void setLateEtaMinutes(Integer lateEtaMinutes) {
    this.lateEtaMinutes = lateEtaMinutes;
  }

  public AvailabilityAfter getAvailabilityAfter() {
    return availabilityAfter;
  }

  public void setAvailabilityAfter(AvailabilityAfter availabilityAfter) {
    this.availabilityAfter = availabilityAfter;
  }

  public Boolean getAvailabilityDifferentSiteOk() {
    return availabilityDifferentSiteOk;
  }

  public void setAvailabilityDifferentSiteOk(Boolean availabilityDifferentSiteOk) {
    this.availabilityDifferentSiteOk = availabilityDifferentSiteOk;
  }

  public String getAvailabilityNote() {
    return availabilityNote;
  }

  public void setAvailabilityNote(String availabilityNote) {
    this.availabilityNote = availabilityNote;
  }

  public SiteEntity getOverrideSite() {
    return overrideSite;
  }

  public void setOverrideSite(SiteEntity overrideSite) {
    this.overrideSite = overrideSite;
  }

  public Instant getOverrideStartAt() {
    return overrideStartAt;
  }

  public void setOverrideStartAt(Instant overrideStartAt) {
    this.overrideStartAt = overrideStartAt;
  }

  public String getOverrideMeetPoint() {
    return overrideMeetPoint;
  }

  public void setOverrideMeetPoint(String overrideMeetPoint) {
    this.overrideMeetPoint = overrideMeetPoint;
  }

  public RecipientSendStatus getSendStatus() {
    return sendStatus;
  }

  public void setSendStatus(RecipientSendStatus sendStatus) {
    this.sendStatus = sendStatus;
  }

  public String getSendError() {
    return sendError;
  }

  public void setSendError(String sendError) {
    this.sendError = sendError;
  }

  public Instant getStandbyClosedAt() {
    return standbyClosedAt;
  }

  public void setStandbyClosedAt(Instant standbyClosedAt) {
    this.standbyClosedAt = standbyClosedAt;
  }

  public RecipientSendStatus getStandbySendStatus() {
    return standbySendStatus;
  }

  public void setStandbySendStatus(RecipientSendStatus standbySendStatus) {
    this.standbySendStatus = standbySendStatus;
  }

  public String getStandbySendError() {
    return standbySendError;
  }

  public void setStandbySendError(String standbySendError) {
    this.standbySendError = standbySendError;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
