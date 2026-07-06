package com.zhiyangyun.care.crm.constant;

/**
 * 合同流程阶段（单一来源）。
 *
 * <p>此前 {@code FLOW_PENDING_ASSESSMENT} 等字符串常量在 CrmContractServiceImpl / CrmLeadServiceImpl /
 * MarketingReportServiceImpl 中各自私有声明，容易随迭代产生分叉。此处统一定义 code 字面量，各处引用
 * {@link #code()} 即可，避免拼写/取值不一致导致的流程判断错误。
 *
 * <p>code 即持久化到 {@code flow_stage} 字段的值，务必与数据库/前端约定保持一致。
 */
public enum ContractFlowStage {
  /** 待评估。 */
  PENDING_ASSESSMENT("PENDING_ASSESSMENT"),
  /** 待选床。 */
  PENDING_BED_SELECT("PENDING_BED_SELECT"),
  /** 待签约。 */
  PENDING_SIGN("PENDING_SIGN"),
  /** 已签约。 */
  SIGNED("SIGNED");

  private final String code;

  ContractFlowStage(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public boolean matches(String value) {
    return code.equalsIgnoreCase(value == null ? null : value.trim());
  }
}
