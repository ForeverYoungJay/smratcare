package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceAllocationMeterValidateResponse {
  private String month;
  private BigDecimal abnormalThreshold;
  private int totalRows;
  private int validRows;
  private int invalidRows;
  private int warningRows;
  private List<RowResult> rows = new ArrayList<>();

  @Data
  public static class RowResult {
    private int rowNo;
    private String building;
    private String floorNo;
    private String roomNo;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private BigDecimal usage;
    private boolean valid;
    private String level;
    private String code;
    private String message;
    private String remark;
  }
}
