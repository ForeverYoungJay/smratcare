package com.zhiyangyun.care.elder.model;

public final class ElderDepartureType {
  private ElderDepartureType() {}

  public static final String NORMAL = "NORMAL";
  public static final String DEATH = "DEATH";

  public static String normalize(String departureType) {
    if (departureType == null || departureType.isBlank()) {
      return null;
    }
    return departureType.trim().toUpperCase();
  }
}
