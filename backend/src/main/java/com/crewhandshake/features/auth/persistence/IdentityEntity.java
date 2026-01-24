package com.crewhandshake.features.auth.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "identities")
public class IdentityEntity {
  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "phone_e164", nullable = false, unique = true, length = 32)
  private String phoneE164;

  protected IdentityEntity() {}

  public IdentityEntity(String phoneE164) {
    this.phoneE164 = phoneE164;
  }

  public UUID getId() {
    return id;
  }

  public String getPhoneE164() {
    return phoneE164;
  }

  public void setPhoneE164(String phoneE164) {
    this.phoneE164 = phoneE164;
  }
}
