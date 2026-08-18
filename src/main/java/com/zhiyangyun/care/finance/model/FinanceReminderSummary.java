package com.zhiyangyun.care.finance.model;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

/** 提醒未处理量，铃铛徽章与提醒中心共用。 */
@Data
public class FinanceReminderSummary {
  private Integer pendingCount = 0;
  private Integer handledCount = 0;
  /** 按类型拆分的未处理量。 */
  private Map<String, Integer> pendingByType = new LinkedHashMap<>();
}
