package com.zhiyangyun.care.compliance.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 导出完成回执：核销一次性凭证并回填实际导出行数。
 */
@Data
public class ExportCompleteRequest {
  @NotBlank(message = "导出凭证不能为空")
  private String exportTicket;
  private Long rowCount;
}
