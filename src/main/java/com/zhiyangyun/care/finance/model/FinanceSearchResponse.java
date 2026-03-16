package com.zhiyangyun.care.finance.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceSearchResponse {
  private String keyword;
  private List<Row> elders = new ArrayList<>();
  private List<Row> bills = new ArrayList<>();
  private List<Row> payments = new ArrayList<>();
  private List<Row> contracts = new ArrayList<>();

  @Data
  public static class Row {
    private String type;
    private Long id;
    private String title;
    private String subtitle;
    private String extra;
    private String actionPath;
    private LocalDateTime time;
  }
}
