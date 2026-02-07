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
}
