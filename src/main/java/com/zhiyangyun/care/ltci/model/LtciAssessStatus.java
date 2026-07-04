package com.zhiyangyun.care.ltci.model;

/** 失能评估状态机常量。 */
public final class LtciAssessStatus {
  private LtciAssessStatus() {}

  public static final String APPLIED = "APPLIED";       // 已申请
  public static final String ACCEPTED = "ACCEPTED";     // 已受理
  public static final String ASSIGNED = "ASSIGNED";     // 已分配评估员
  public static final String SCORING = "SCORING";       // 评估打分中
  public static final String JUDGED = "JUDGED";         // 已判级
  public static final String NOTIFIED = "NOTIFIED";     // 结果已告知
  public static final String DISPUTED = "DISPUTED";     // 争议中
  public static final String REVIEWING = "REVIEWING";   // 复核中
  public static final String EFFECTIVE = "EFFECTIVE";   // 已生效
  public static final String EXPIRED = "EXPIRED";       // 已到期
  public static final String CANCELLED = "CANCELLED";   // 已取消

  public static final String TYPE_FIRST = "FIRST";
  public static final String TYPE_REVIEW = "REVIEW";
  public static final String TYPE_DISPUTE = "DISPUTE";
}
