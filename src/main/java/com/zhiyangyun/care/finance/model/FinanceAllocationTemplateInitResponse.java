package com.zhiyangyun.care.finance.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceAllocationTemplateInitResponse {
  private String month;
  private int createdCount;
  private int updatedCount;
  private List<String> configKeys = new ArrayList<>();
}
