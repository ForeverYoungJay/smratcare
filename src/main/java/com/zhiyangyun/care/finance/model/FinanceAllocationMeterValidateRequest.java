package com.zhiyangyun.care.finance.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceAllocationMeterValidateRequest {
  private String month;

  @DecimalMin(value = "0", message = "abnormalThreshold 不能小于0")
  private BigDecimal abnormalThreshold;

  @Valid
  @NotNull(message = "rows 不能为空")
  private List<Row> rows = new ArrayList<>();

  @Data
  public static class Row {
    private String building;
    private String floorNo;
    private String roomNo;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private String remark;
  }
}
