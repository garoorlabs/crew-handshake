package com.crewhandshake.features.auth.persistence;

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
@Table(name = "otp_codes")
public class OtpCodeEntity {
  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "identity_id", nullable = false)
  private IdentityEntity identity;

  @Column(name = "code_hash", nullable = false, length = 200)
  private String codeHash;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "attempts", nullable = false)
  private int attempts;

  @Column(name = "max_attempts", nullable = false)
  private int maxAttempts;

  @Column(name = "consumed_at")
  private Instant consumedAt;

  protected OtpCodeEntity() {}

  public OtpCodeEntity(IdentityEntity identity, String codeHash, Instant createdAt, Instant expiresAt, int maxAttempts) {
    this.identity = identity;
    this.codeHash = codeHash;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
    this.maxAttempts = maxAttempts;
    this.attempts = 0;
  }

  public UUID getId() {
    return id;
  }

  public IdentityEntity getIdentity() {
    return identity;
  }

  public String getCodeHash() {
    return codeHash;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public int getAttempts() {
    return attempts;
  }

  public int getMaxAttempts() {
    return maxAttempts;
  }

  public Instant getConsumedAt() {
    return consumedAt;
  }

  public void incrementAttempts() {
    this.attempts += 1;
  }

  public void markConsumed(Instant at) {
    this.consumedAt = at;
  }
}
