package com.zhiyangyun.care.compliance.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 导出二次确认请求：签发一次性导出凭证。
 */
@Data
public class ExportConfirmRequest {
  /** 导出模块标识，如 ELDER_LIST / FAMILY_LIST / SENSITIVE_ACCESS_LOG */
  @NotBlank(message = "导出模块不能为空")
  private String module;
  /** 导出范围描述（筛选条件等） */
  private String scope;
  /** 导出用途（二次确认时填写） */
  @NotBlank(message = "导出用途不能为空")
  private String purpose;
  /** 预估行数（可选） */
  private Long estimatedRows;
}
