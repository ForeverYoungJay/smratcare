package com.zhiyangyun.care.finance.model;

public final class FeeWorkflowConstants {
  private FeeWorkflowConstants() {}

  public static final String AUDIT_PENDING = "PENDING";
  public static final String AUDIT_APPROVED = "APPROVED";
  public static final String AUDIT_REJECTED = "REJECTED";

  public static final String SETTLEMENT_PENDING_CONFIRM = "PENDING_CONFIRM";
  public static final String SETTLEMENT_PROCESSING = "PROCESSING";
  public static final String SETTLEMENT_SETTLED = "SETTLED";
  public static final String SETTLEMENT_CANCELLED = "CANCELLED";
}
