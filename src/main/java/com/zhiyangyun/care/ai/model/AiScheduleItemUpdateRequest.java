package com.zhiyangyun.care.ai.model;

import lombok.Data;

/** 智能排班：人工调整明细请求（换人或换班次）。 */
@Data
public class AiScheduleItemUpdateRequest {
  private Long staffId;
  private String shiftCode;
}
