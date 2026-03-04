package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceRoomOpsDetailResponse {
  private String period;
  private String building;
  private String room;
  private BigDecimal totalIncome;
  private BigDecimal totalCost;
  private BigDecimal totalNetAmount;
  private Integer totalOccupiedBeds;
  private Integer totalEmptyBeds;
  private List<Row> rows = new ArrayList<>();

  @Data
  public static class Row {
    private String building;
    private String floorNo;
    private String roomNo;
    private BigDecimal income;
    private BigDecimal cost;
    private BigDecimal netAmount;
    private Integer occupiedBeds;
    private Integer emptyBeds;
  }
}
