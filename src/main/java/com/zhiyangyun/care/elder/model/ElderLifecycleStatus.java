package com.zhiyangyun.care.elder.model;

public final class ElderLifecycleStatus {
  private ElderLifecycleStatus() {}

  public static final String INTENT = "INTENT";
  public static final String TRIAL = "TRIAL";
  public static final String IN_HOSPITAL = "IN_HOSPITAL";
  public static final String OUTING = "OUTING";
  public static final String MEDICAL_OUTING = "MEDICAL_OUTING";
  public static final String DISCHARGE_PENDING = "DISCHARGE_PENDING";
  public static final String DISCHARGED = "DISCHARGED";
  public static final String DECEASED = "DECEASED";

  public static String normalize(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    return status.trim().toUpperCase();
  }

  public static String fromLegacyStatus(Integer status, String departureType) {
    if (status == null) {
      return INTENT;
    }
    if (status == ElderStatus.IN_HOSPITAL) {
      return IN_HOSPITAL;
    }
    if (status == ElderStatus.OUTING) {
      return OUTING;
    }
    if (status == ElderStatus.DISCHARGED) {
      return ElderDepartureType.DEATH.equalsIgnoreCase(departureType) ? DECEASED : DISCHARGED;
    }
    return INTENT;
  }

  public static Integer toLegacyStatus(String lifecycleStatus, Integer fallback) {
    String normalized = normalize(lifecycleStatus);
    if (normalized == null) {
      return fallback;
    }
    return switch (normalized) {
      case INTENT -> null;
      case IN_HOSPITAL, TRIAL, DISCHARGE_PENDING -> ElderStatus.IN_HOSPITAL;
      case OUTING, MEDICAL_OUTING -> ElderStatus.OUTING;
      case DISCHARGED, DECEASED -> ElderStatus.DISCHARGED;
      default -> fallback;
    };
  }
}
