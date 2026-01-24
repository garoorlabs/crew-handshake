package com.crewhandshake.features.auth.persistence;

import com.crewhandshake.common.tenant.DispatchAuthority;
import com.crewhandshake.common.tenant.PayrollFrequency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "companies")
public class CompanyEntity {
  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(name = "default_language", nullable = false, length = 8)
  private String defaultLanguage;

  @Enumerated(EnumType.STRING)
  @Column(name = "payroll_frequency", nullable = false, length = 16)
  private PayrollFrequency payrollFrequency;

  @Enumerated(EnumType.STRING)
  @Column(name = "payroll_cutoff_day", nullable = false, length = 16)
  private DayOfWeek payrollCutoffDay;

  @Column(name = "standby_cutoff_time", nullable = false)
  private LocalTime standbyCutoffTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "dispatch_authority", nullable = false, length = 16)
  private DispatchAuthority dispatchAuthority;

  protected CompanyEntity() {}

  public CompanyEntity(String name) {
    this.name = name;
    this.defaultLanguage = "en";
    this.payrollFrequency = PayrollFrequency.WEEKLY;
    this.payrollCutoffDay = DayOfWeek.FRIDAY;
    this.standbyCutoffTime = LocalTime.of(18, 0);
    this.dispatchAuthority = DispatchAuthority.HYBRID;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDefaultLanguage() {
    return defaultLanguage;
  }

  public void setDefaultLanguage(String defaultLanguage) {
    this.defaultLanguage = defaultLanguage;
  }

  public PayrollFrequency getPayrollFrequency() {
    return payrollFrequency;
  }

  public void setPayrollFrequency(PayrollFrequency payrollFrequency) {
    this.payrollFrequency = payrollFrequency;
  }

  public DayOfWeek getPayrollCutoffDay() {
    return payrollCutoffDay;
  }

  public void setPayrollCutoffDay(DayOfWeek payrollCutoffDay) {
    this.payrollCutoffDay = payrollCutoffDay;
  }

  public LocalTime getStandbyCutoffTime() {
    return standbyCutoffTime;
  }

  public void setStandbyCutoffTime(LocalTime standbyCutoffTime) {
    this.standbyCutoffTime = standbyCutoffTime;
  }

  public DispatchAuthority getDispatchAuthority() {
    return dispatchAuthority;
  }

  public void setDispatchAuthority(DispatchAuthority dispatchAuthority) {
    this.dispatchAuthority = dispatchAuthority;
  }
}
