package com.zhiyangyun.care.crm.constant;

/**
 * 合同变更工作流状态（单一来源）。持久化到 {@code change_workflow_status} 字段。
 */
public enum ContractChangeStatus {
  /** 无变更。 */
  NONE("NONE"),
  /** 变更进行中。 */
  IN_PROGRESS("IN_PROGRESS"),
  /** 待审批。 */
  PENDING_APPROVAL("PENDING_APPROVAL"),
  /** 已通过。 */
  APPROVED("APPROVED"),
  /** 已驳回。 */
  REJECTED("REJECTED");

  private final String code;

  ContractChangeStatus(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public boolean matches(String value) {
    return code.equalsIgnoreCase(value == null ? null : value.trim());
  }
}
