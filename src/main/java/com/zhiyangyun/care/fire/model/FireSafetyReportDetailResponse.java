package com.zhiyangyun.care.fire.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FireSafetyReportDetailResponse {
  private Long totalCount = 0L;
  private Long dailyCompletedCount = 0L;
  private Long monthlyCompletedCount = 0L;
  private Long dutyRecordCount = 0L;
  private Long handoverPunchCount = 0L;
  private Long equipmentUpdateCount = 0L;
  private Long equipmentAgingDisposalCount = 0L;
  private List<FireSafetyReportRecordItem> records = new ArrayList<>();

  @Data
  public static class FireSafetyReportRecordItem {
    private Long id;
    private String recordType;
    private String title;
    private String location;
    private String inspectorName;
    private String checkTime;
    private String status;
    private String scanCompletedAt;
    private String dutyRecord;
    private String handoverPunchTime;
    private String equipmentBatchNo;
    private String equipmentUpdateNote;
    private String equipmentAgingDisposal;
    private String issueDescription;
    private String actionTaken;
  }
}
