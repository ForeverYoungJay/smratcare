package com.zhiyangyun.care.compliance.annotation;

/**
 * 敏感字段类型：决定 {@link SensitiveField} 序列化时采用的脱敏规则。
 */
public enum SensitiveType {
  /** 姓名：张* */
  NAME,
  /** 手机号：138****1234 */
  PHONE,
  /** 身份证号：3301********123X */
  ID_CARD,
  /** 地址：保留前 6 个字符 */
  ADDRESS,
  /** 银行卡：仅保留后 4 位 */
  BANK_CARD,
  /** 病情/健康摘要：保留前 2 个字符，其余打码 */
  MEDICAL_SUMMARY
}
