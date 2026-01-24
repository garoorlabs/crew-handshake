package com.crewhandshake.features.foreman.persistence;

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
@Table(name = "audit_logs")
public class AuditLogEntity {
  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private CompanyEntity company;

  @ManyToOne(optional = false)
  @JoinColumn(name = "actor_membership_id", nullable = false)
  private MembershipEntity actorMembership;

  @Column(name = "action_type", nullable = false, length = 64)
  private String actionType;

  @Column(name = "entity_type", nullable = false, length = 64)
  private String entityType;

  @Column(name = "entity_id", nullable = false)
  private UUID entityId;

  @Column(name = "details_json")
  private String detailsJson;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected AuditLogEntity() {}

  public AuditLogEntity(CompanyEntity company,
                        MembershipEntity actorMembership,
                        String actionType,
                        String entityType,
                        UUID entityId,
                        String detailsJson,
                        Instant createdAt) {
    this.company = company;
    this.actorMembership = actorMembership;
    this.actionType = actionType;
    this.entityType = entityType;
    this.entityId = entityId;
    this.detailsJson = detailsJson;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public CompanyEntity getCompany() {
    return company;
  }

  public MembershipEntity getActorMembership() {
    return actorMembership;
  }

  public String getActionType() {
    return actionType;
  }

  public String getEntityType() {
    return entityType;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public String getDetailsJson() {
    return detailsJson;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
