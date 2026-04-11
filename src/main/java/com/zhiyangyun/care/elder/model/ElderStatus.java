package com.zhiyangyun.care.elder.model;

public final class ElderStatus {
  private ElderStatus() {}

  public static final int IN_HOSPITAL = 1;
  public static final int OUTING = 2;
  public static final int DISCHARGED = 3;

  public static String text(Integer status) {
    if (status == null) {
      return "";
    }
    if (status == IN_HOSPITAL) {
      return "在院";
    }
    if (status == OUTING) {
      return "外出";
    }
    if (status == DISCHARGED) {
      return "离院";
    }
    return String.valueOf(status);
  }
}
