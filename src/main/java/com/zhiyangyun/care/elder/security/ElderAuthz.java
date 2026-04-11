package com.zhiyangyun.care.elder.security;

import com.zhiyangyun.care.auth.security.AuthContext;
import org.springframework.stereotype.Component;

@Component("elderAuthz")
public class ElderAuthz {
  public boolean canReadElder() {
    return hasAnyRole(
        "HR_EMPLOYEE", "HR_MINISTER",
        "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER",
        "NURSING_EMPLOYEE", "NURSING_MINISTER",
        "FINANCE_EMPLOYEE", "FINANCE_MINISTER",
        "MARKETING_EMPLOYEE", "MARKETING_MINISTER",
        "DIRECTOR", "SYS_ADMIN", "ADMIN");
  }

  public boolean canWriteElder() {
    return hasAnyRole(
        "HR_EMPLOYEE", "HR_MINISTER",
        "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER",
        "NURSING_EMPLOYEE", "NURSING_MINISTER",
        "DIRECTOR", "SYS_ADMIN", "ADMIN");
  }

  public boolean canReadBed() {
    return hasAnyRole(
        "HR_EMPLOYEE", "HR_MINISTER",
        "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER",
        "NURSING_EMPLOYEE", "NURSING_MINISTER",
        "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER",
        "DIRECTOR", "SYS_ADMIN", "ADMIN");
  }

  public boolean canWriteBed() {
    return hasAnyRole("LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN");
  }

  public boolean canReadResidence() {
    return hasAnyRole(
        "HR_EMPLOYEE", "HR_MINISTER",
        "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER",
        "NURSING_EMPLOYEE", "NURSING_MINISTER",
        "FINANCE_EMPLOYEE", "FINANCE_MINISTER",
        "MARKETING_EMPLOYEE", "MARKETING_MINISTER",
        "DIRECTOR", "SYS_ADMIN", "ADMIN");
  }

  public boolean canManageOuting() {
    return hasAnyRole(
        "HR_EMPLOYEE", "HR_MINISTER",
        "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER",
        "NURSING_EMPLOYEE", "NURSING_MINISTER",
        "DIRECTOR", "SYS_ADMIN", "ADMIN");
  }

  public boolean canManageTrial() {
    return hasAnyRole(
        "HR_EMPLOYEE", "HR_MINISTER",
        "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER",
        "NURSING_EMPLOYEE", "NURSING_MINISTER",
        "MARKETING_EMPLOYEE", "MARKETING_MINISTER",
        "DIRECTOR", "SYS_ADMIN", "ADMIN");
  }

  public boolean canManageDischarge() {
    return hasAnyRole(
        "HR_EMPLOYEE", "HR_MINISTER",
        "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER",
        "NURSING_EMPLOYEE", "NURSING_MINISTER",
        "DIRECTOR", "SYS_ADMIN", "ADMIN");
  }

  public boolean canApproveDischarge() {
    return hasAnyRole("MEDICAL_MINISTER", "NURSING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN");
  }

  public boolean canUseFinanceWorkbench() {
    return hasAnyRole("FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN");
  }

  public boolean canManageFinanceWorkbenchAdmin() {
    return hasAnyRole("FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN");
  }

  private boolean hasAnyRole(String... roles) {
    for (String role : roles) {
      if (AuthContext.hasRole(role)) {
        return true;
      }
    }
    return false;
  }
}
