package com.crewhandshake.features.auth.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_request_attempts")
public class OtpRequestAttemptEntity {
  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "phone_e164", nullable = false, length = 32)
  private String phoneE164;

  @Column(name = "ip_address", nullable = false, length = 64)
  private String ipAddress;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected OtpRequestAttemptEntity() {}

  public OtpRequestAttemptEntity(String phoneE164, String ipAddress, Instant createdAt) {
    this.phoneE164 = phoneE164;
    this.ipAddress = ipAddress;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getPhoneE164() {
    return phoneE164;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
