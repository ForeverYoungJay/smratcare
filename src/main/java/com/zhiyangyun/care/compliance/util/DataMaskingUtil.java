package com.zhiyangyun.care.compliance.util;

/**
 * 敏感个人信息脱敏工具。用于对外展示/日志留痕时对身份证、手机号、姓名、银行卡等做掩码处理。
 * 遵循最小披露原则：默认仅保留必要的首尾字符。
 */
public final class DataMaskingUtil {
  private DataMaskingUtil() {}

  private static final String MASK = "****";

  /** 手机号：保留前 3 后 4，如 138****1234。 */
  public static String maskPhone(String phone) {
    if (phone == null) {
      return null;
    }
    String v = phone.trim();
    if (v.length() < 7) {
      return maskAll(v);
    }
    return v.substring(0, 3) + MASK + v.substring(v.length() - 4);
  }

  /** 身份证：保留前 4 后 4，中间掩码，如 3101********1234。 */
  public static String maskIdCard(String idCard) {
    if (idCard == null) {
      return null;
    }
    String v = idCard.trim();
    if (v.length() <= 8) {
      return maskAll(v);
    }
    return v.substring(0, 4) + "********" + v.substring(v.length() - 4);
  }

  /** 姓名：保留姓氏，其余以 * 代替，如 张**。 */
  public static String maskName(String name) {
    if (name == null || name.isEmpty()) {
      return name;
    }
    String v = name.trim();
    if (v.length() == 1) {
      return v;
    }
    return v.charAt(0) + "*".repeat(v.length() - 1);
  }

  /** 银行卡：仅保留后 4 位。 */
  public static String maskBankCard(String card) {
    if (card == null) {
      return null;
    }
    String v = card.replaceAll("\\s", "");
    if (v.length() <= 4) {
      return maskAll(v);
    }
    return MASK + " " + v.substring(v.length() - 4);
  }

  /** 通用地址：保留前 6 个字符，其余掩码。 */
  public static String maskAddress(String address) {
    if (address == null) {
      return null;
    }
    String v = address.trim();
    if (v.length() <= 6) {
      return v;
    }
    return v.substring(0, 6) + MASK;
  }

  /** 病情/健康摘要：保留前 2 个字符，其余打码，避免病情细节外泄。 */
  public static String maskMedicalSummary(String summary) {
    if (summary == null) {
      return null;
    }
    String v = summary.trim();
    if (v.isEmpty()) {
      return v;
    }
    if (v.length() <= 2) {
      return "*".repeat(v.length());
    }
    return v.substring(0, 2) + MASK;
  }

  private static String maskAll(String v) {
    if (v == null || v.isEmpty()) {
      return v;
    }
    if (v.length() <= 2) {
      return "*".repeat(v.length());
    }
    return v.charAt(0) + "*".repeat(v.length() - 2) + v.charAt(v.length() - 1);
  }
}
