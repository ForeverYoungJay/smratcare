package com.zhiyangyun.care.crm.model.report;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 市场人员协作与到访汇总：后端全量统计，替代前端取一页再计数。 */
@Data
public class MarketingStaffActivityResponse {
  /** 区间内实际到访（已到访/已完成）的预约次数 */
  private long arrivedVisitCount;
  /** 区间内实际到访的去重客户数 */
  private long arrivedLeadCount;
  private List<StaffRow> staff = new ArrayList<>();

  @Data
  public static class StaffRow {
    private String name;
    private long leads;
    private long visits;
    private long contracts;
    private long overdue;
  }
}
