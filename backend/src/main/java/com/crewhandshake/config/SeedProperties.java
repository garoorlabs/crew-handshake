package com.crewhandshake.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seed")
public class SeedProperties {
  private boolean enabled = true;
  private String companyName = "Crew Handshake Demo";
  private String adminPhone = "+14155550100";
  private String adminName = "Admin User";
  private String foremanPhone = "+14155550101";
  private String foremanName = "Foreman User";
  private String workerPhone = "+14155550102";
  private String workerName = "Worker User";
  private String crewName = "Crew A";
  private String siteName = "Main Site";
  private String siteAddress = "123 Main St";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getAdminPhone() {
    return adminPhone;
  }

  public void setAdminPhone(String adminPhone) {
    this.adminPhone = adminPhone;
  }

  public String getAdminName() {
    return adminName;
  }

  public void setAdminName(String adminName) {
    this.adminName = adminName;
  }

  public String getForemanPhone() {
    return foremanPhone;
  }

  public void setForemanPhone(String foremanPhone) {
    this.foremanPhone = foremanPhone;
  }

  public String getForemanName() {
    return foremanName;
  }

  public void setForemanName(String foremanName) {
    this.foremanName = foremanName;
  }

  public String getWorkerPhone() {
    return workerPhone;
  }

  public void setWorkerPhone(String workerPhone) {
    this.workerPhone = workerPhone;
  }

  public String getWorkerName() {
    return workerName;
  }

  public void setWorkerName(String workerName) {
    this.workerName = workerName;
  }

  public String getCrewName() {
    return crewName;
  }

  public void setCrewName(String crewName) {
    this.crewName = crewName;
  }

  public String getSiteName() {
    return siteName;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
  }

  public String getSiteAddress() {
    return siteAddress;
  }

  public void setSiteAddress(String siteAddress) {
    this.siteAddress = siteAddress;
  }
}
