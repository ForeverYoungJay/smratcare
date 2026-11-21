package com.zhiyangyun.care.bill.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BillGenerateResponse {
  private String billMonth;
  private int generatedCount;
  private int skippedCount;
  private List<Long> billIds = new ArrayList<>();
  private String message;
  /** 计费标准缺失提示：缺失时对应费用按 0 计，需财务补齐后重新生成 */
  private List<String> warnings = new ArrayList<>();
}
