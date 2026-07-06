package com.zhiyangyun.care.schedule.model;

import java.util.List;
import lombok.Data;

@Data
public class AttendanceDashboardOverviewResponse {
  private Long staffId;
  private String staffName;
  private String month;
  private String seasonType;
  private String expectedWorkStart;
  private String expectedWorkEnd;
  private Integer lateGraceMinutes;
  private Integer earlyLeaveGraceMinutes;
  private Integer outingMaxMinutes;
  private Long onDutyDays;
  private Long leaveDays;
  private Long lateCount;
  private Long earlyLeaveCount;
  private Long abnormalCount;
  private Integer totalOutingMinutes;
  private Integer totalLunchBreakMinutes;
  private String todayStatus;
  private String todayStatusLabel;
  /** 建议的下一次打卡动作（IN/OUT/START_LUNCH/END_LUNCH/START_OUTING/END_OUTING），无可用动作时为空。 */
  private String nextPunchAction;
  /** 下一次打卡动作的中文文案，供移动端工作台一键打卡按钮直接展示。 */
  private String nextPunchActionLabel;
  private List<AttendanceDashboardDayItem> days;
}
